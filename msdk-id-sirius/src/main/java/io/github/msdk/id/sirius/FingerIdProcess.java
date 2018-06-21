package io.github.msdk.id.sirius;/*
 * (C) Copyright 2015-2017 by MSDK Development Team
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.fp.MaskedFingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.ProbabilityFingerprint;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import de.unijena.bioinf.babelms.json.FTJsonWriter;
import de.unijena.bioinf.babelms.ms.JenaMsWriter;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.utils.systemInfo.SystemInformation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;

import java.util.Objects;
import java.util.concurrent.TimeoutException;
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

  public FingerIdProcess(final Ms2Experiment experiment, final IdentificationResult result, final FTree tree, final MaskedFingerprintVersion version, final EnumSet<PredictorType> predicors) {

  }


  private URIBuilder getFingerIdURI(String path) {
    if (path == null)
      path = "";
    URIBuilder builder = null;
    builder.setPath("/csi-finger-id-" + FingerIdProcess.fingeridVersion() + path);

    return builder;
  }

  //TODO: make it better
  private static String fingeridVersion() {
    return "1.1.2";
  }

  public ProbabilityFingerprint 

  public FingerIdJob submitJob(final Ms2Experiment experiment, final IdentificationResult result, final FTree ftree, final MaskedFingerprintVersion version, final EnumSet<PredictorType> types) throws IOException, URISyntaxException{
    final HttpPost post = new HttpPost(getFingerIdURI("/webapi/predict.json").build());
    final String stringMs, jsonTree;
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

    final UrlEncodedFormEntity params = new UrlEncodedFormEntity(
        Arrays.asList(ms, tree, predictor, UID));
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

    return null;
  }

  public ProbabilityFingerprint processFingerIdJob(FingerIdJob job) throws URISyntaxException, InterruptedException, TimeoutException {
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
  }

  class FingerIdJob {
    public long jobId;
    public String securityToken;
    public MaskedFingerprintVersion v;
    public ProbabilityFingerprint prediction;
    public String errorMessage;

    public FingerIdJob(long jobId, String securityToken, MaskedFingerprintVersion version) {
      this.jobId = jobId;
      this.securityToken = securityToken;
      this.v = version;
    }
  }

}
