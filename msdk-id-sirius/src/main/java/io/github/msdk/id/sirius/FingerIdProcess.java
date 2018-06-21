package io.github.msdk.id.sirius;/*
 * (C) Copyright 2015-2018 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

import com.google.gson.Gson;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.fp.CdkFingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.MaskedFingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.ProbabilityFingerprint;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import de.unijena.bioinf.babelms.json.FTJsonWriter;
import de.unijena.bioinf.babelms.ms.JenaMsWriter;
import de.unijena.bioinf.babelms.utils.Base64;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.utils.systemInfo.SystemInformation;
import gnu.trove.list.array.TDoubleArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import javax.print.URIException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.LoggerFactory;

public class FingerIdProcess {

  private CloseableHttpClient client = HttpClients.createSystem();
  private final BasicNameValuePair UID = new BasicNameValuePair("uid", SystemInformation.generateSystemKey());

  public FingerIdProcess() {

  }

  public ProbabilityFingerprint useThis(Ms2Experiment experiment, IdentificationResult result) {
    CdkFingerprintVersion version = CdkFingerprintVersion.withECFP();
    MaskedFingerprintVersion.Builder maskedBuiled = MaskedFingerprintVersion.buildMaskFor(version);
    maskedBuiled.disableAll();

    ProbabilityFingerprint print = null;

    try {
      FingerIdJob job = submitJob(experiment, result, maskedBuiled.toMask());
      print = processFingerIdJob(job);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return print;
  }


  private URIBuilder getFingerIdURI(String path) throws URISyntaxException {
    if (path == null)
      path = "";
    URIBuilder builder = new URIBuilder("https://www.csi-fingerid.uni-jena.de");
    builder.setPath("/csi-fingerid-" + FingerIdProcess.fingeridVersion() + path);

    return builder;
  }

  //TODO: make it better
  private static String fingeridVersion() {
    return "1.1.2";
  }

  public FingerIdJob submitJob(final Ms2Experiment experiment, final IdentificationResult result, final MaskedFingerprintVersion version) throws IOException, URISyntaxException{
    final HttpPost post = new HttpPost(getFingerIdURI("/webapi/predict.json").build());
    final String stringMs, jsonTree;
    final FTree ftree = result.getResolvedTree();
    {
      final JenaMsWriter writer = new JenaMsWriter();
      final StringWriter sw = new StringWriter();
      try (final BufferedWriter bw = new BufferedWriter(sw)) {
        writer.write(bw, experiment);
      }
      stringMs = sw.toString();
    }
    {
      final FTJsonWriter writer = new FTJsonWriter();
      final StringWriter sw = new StringWriter();
      writer.writeTree(sw, ftree);
      jsonTree = sw.toString();
    }

    final NameValuePair ms = new BasicNameValuePair("ms", stringMs);
    final NameValuePair tree = new BasicNameValuePair("ft", jsonTree);

    final NameValuePair predictor = new BasicNameValuePair("predictors", getPredictor(experiment.getPrecursorIonType()));

    final UrlEncodedFormEntity params = new UrlEncodedFormEntity(Arrays.asList(ms, tree, predictor, UID));
    post.setEntity(params);

    final String securityToken;
    final long jobId;
    // SUBMIT JOB
    try (CloseableHttpResponse response = client.execute(post)) {
      if (response.getStatusLine().getStatusCode() == 200) {
        final Gson gson = new Gson();
        try {
          GetResponse getResponse = gson.fromJson(new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ContentType
              .getOrDefault(response.getEntity()).getCharset())), GetResponse.class);
          securityToken = getResponse.securityToken;
          jobId = getResponse.jobId;
          return new FingerIdJob(jobId, securityToken, version);
        } finally {

        }
      } else {
        RuntimeException re = new RuntimeException(response.getStatusLine().getReasonPhrase());
        LoggerFactory.getLogger(this.getClass()).debug("Submitting Job failed", re);
        throw re;
      }
    } finally {
    }
  }

  public ProbabilityFingerprint processFingerIdJob(FingerIdJob job) throws URISyntaxException, InterruptedException, TimeoutException, IOException {
    // RECEIVE RESULTS
    new HttpGet(getFingerIdURI("/webapi/job.json").setParameter("jobId", String.valueOf(job.jobId)).setParameter("securityToken", job.securityToken).build());
    for (int k = 0; k < 600; ++k) {
      Thread.sleep(3000 + 30 * k);
      if (updateJobStatus(job)) {
        return job.prediction;
      } else if (Objects.equals(job.state, "CRASHED")) {
        throw new RuntimeException("Job crashed: " + (job.errorMessage != null ? job.errorMessage : ""));
      }
    }
    throw new TimeoutException("Reached timeout");
  }

  private boolean updateJobStatus(FingerIdJob job) throws URISyntaxException, IOException {
    final HttpGet get = new HttpGet(getFingerIdURI("/webapi/job.json").setParameter("jobId", String.valueOf(job.jobId)).setParameter("securityToken", job.securityToken).build());
    try (CloseableHttpResponse response = client.execute(get)) {
      Gson gson = new Gson();
      try {
        GetResponse getResponse = gson.fromJson(new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ContentType.getOrDefault(response.getEntity()).getCharset())), GetResponse.class);
        if (getResponse.prediction != null){
          getResponse.plattBytes = Base64.decode(getResponse.prediction);
          final double[] platts = parseBinaryToDoubles(getResponse.plattBytes);
          job.prediction = new ProbabilityFingerprint(job.v, platts);

          if (getResponse.iokrVector != null) {
            getResponse.iokrBytes = Base64.decode(getResponse.iokrVector);
            job.iokrVector = parseBinaryToDoubles(getResponse.iokrBytes);
          }

          return true;
        } else {
          job.state = getResponse.state != null ? getResponse.state : "SUBMITTED";
        }
        if (getResponse.errors != null) {
          job.errorMessage = getResponse.errors;
        }
      } finally {
        //TODO: stuff
      }
    } catch (Throwable t) {
      LoggerFactory.getLogger(this.getClass()).error("Error when updating job #" + job.jobId, t);
      throw (t);
    }
    return false;
  }

  double[] parseBinaryToDoubles(byte[] bytes) {
    final TDoubleArrayList data = new TDoubleArrayList(2000);
    final ByteBuffer buf = ByteBuffer.wrap(bytes);
    buf.order(ByteOrder.LITTLE_ENDIAN);
    while (buf.position() < buf.limit()) {
      data.add(buf.getDouble());
    }
    return data.toArray();
  }

  /* MAGIC CONSTATNS  */
  private String getPredictor(PrecursorIonType precursorIonType) {
    int charge = precursorIonType.getIonization().getCharge();
    if (charge > 0)
      return "1";
    return "4";
  }

  class GetResponse {
    public String securityToken;
    public int jobId;
    byte[]  plattBytes;
    byte[] iokrBytes;

    String iokrVector;
    String prediction;
    String errors;
    String state;
  }

  class FingerIdJob {
    public long jobId;
    public String securityToken;
    public MaskedFingerprintVersion v;
    public ProbabilityFingerprint prediction;
    public String errorMessage;
    public String state;
    public double[] iokrVector;

    public FingerIdJob(long jobId, String securityToken, MaskedFingerprintVersion version) {
      this.jobId = jobId;
      this.securityToken = securityToken;
      this.v = version;
    }
  }

}
