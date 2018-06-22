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
import de.unijena.bioinf.ChemistryBase.algorithm.Scored;
import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.fp.CdkFingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.FingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.MaskedFingerprintVersion;
import de.unijena.bioinf.ChemistryBase.fp.PredictionPerformance;
import de.unijena.bioinf.ChemistryBase.fp.ProbabilityFingerprint;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import de.unijena.bioinf.babelms.json.FTJsonWriter;
import de.unijena.bioinf.babelms.ms.JenaMsWriter;
import de.unijena.bioinf.babelms.utils.Base64;
import de.unijena.bioinf.chemdb.BioFilter;
import de.unijena.bioinf.chemdb.CompoundCandidateChargeLayer;
import de.unijena.bioinf.chemdb.CompoundCandidateChargeState;
import de.unijena.bioinf.chemdb.FingerprintCandidate;
import de.unijena.bioinf.chemdb.RESTDatabase;
import de.unijena.bioinf.chemdb.SearchStructureByFormula;
import de.unijena.bioinf.fingerid.blast.CovarianceScoring;
import de.unijena.bioinf.fingerid.blast.Fingerblast;
import de.unijena.bioinf.fingerid.blast.FingerblastScoringMethod;
import de.unijena.bioinf.fingerid.blast.ScoringMethodFactory;
import de.unijena.bioinf.fingerid.predictor_types.PredictorType;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.utils.systemInfo.SystemInformation;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import org.apache.http.HttpEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FingerIdWebProcess {

  private final static CloseableHttpClient client = HttpClients.createSystem(); // Threadsafe
  private final static Logger logger = LoggerFactory.getLogger(FingerIdWebProcess.class);
  private final static BasicNameValuePair UID = new BasicNameValuePair("uid", SystemInformation.generateSystemKey());
  private final static String FINGERID_SOURCE = "https://www.csi-fingerid.uni-jena.de";
  private final static String FINGERID_VERSION = "1.1.2";
  private final static SearchStructureByFormula searchDB = new RESTDatabase(BioFilter.ALL);

  private final Ms2Experiment experiment;
  private final IdentificationResult siriusResult;


  public FingerIdWebProcess(Ms2Experiment experiment, IdentificationResult siriusResult) {
    this.experiment = experiment;
    this.siriusResult = siriusResult;
  }

  private URIBuilder getFingerIdURI(String path) throws URISyntaxException {
    if (path == null)
      path = "";
    URIBuilder builder = new URIBuilder(FINGERID_SOURCE);
    builder.setPath("/csi-fingerid-" + FingerIdWebProcess.fingerIdVersion() + path);

    return builder;
  }

  private List<FingerprintCandidate> getCandidates() throws de.unijena.bioinf.chemdb.DatabaseException {
    PrecursorIonType ionType = experiment.getPrecursorIonType();
    MolecularFormula formula = siriusResult.getMolecularFormula();

    final CompoundCandidateChargeState chargeState = CompoundCandidateChargeState.getFromPrecursorIonType(ionType);
    if (chargeState != CompoundCandidateChargeState.NEUTRAL_CHARGE) {
      final List<FingerprintCandidate> intrinsic = searchDB.lookupStructuresAndFingerprintsByFormula(formula);
      intrinsic.removeIf((f)->!f.hasChargeState(CompoundCandidateChargeLayer.Q_LAYER, chargeState));
      // all intrinsic formulas have to contain a p layer?
      final MolecularFormula hydrogen = MolecularFormula.parse("H");
      final List<FingerprintCandidate> protonated = searchDB.lookupStructuresAndFingerprintsByFormula(ionType.getCharge()>0 ? formula.subtract(hydrogen) : formula.add(hydrogen));
      protonated.removeIf((f)->!f.hasChargeState(CompoundCandidateChargeLayer.P_LAYER, chargeState));

      intrinsic.addAll(protonated);
      return intrinsic;
    } else {
      final List<FingerprintCandidate> candidates = searchDB.lookupStructuresAndFingerprintsByFormula(formula);
      candidates.removeIf((f)->!f.hasChargeState(CompoundCandidateChargeLayer.P_LAYER, CompoundCandidateChargeState.NEUTRAL_CHARGE));
      return candidates;
    }
  }

  private PredictionPerformance[] getStatistics(PredictorType predictorType, final TIntArrayList fingerprintIndizes) throws IOException {
    fingerprintIndizes.clear();
    final HttpGet get;
    try {
      get = new HttpGet(getFingerIdURI("/webapi/statistics.csv").setParameter("predictor", predictorType.toBitsAsString()).build());
    } catch (URISyntaxException e) {
      LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    final TIntArrayList[] lists = new TIntArrayList[5];
    ArrayList<PredictionPerformance> performances = new ArrayList<>();
    try (CloseableHttpResponse response = client.execute(get)) {
      HttpEntity e = response.getEntity();
      final BufferedReader br = new BufferedReader(new InputStreamReader(e.getContent(), ContentType.getOrDefault(e).getCharset()));
      String line; //br.readLine();
      while ((line = br.readLine()) != null) {
        String[] tabs = line.split("\t");
        final int index = Integer.parseInt(tabs[0]);
        PredictionPerformance p = new PredictionPerformance(
            Double.parseDouble(tabs[1]),
            Double.parseDouble(tabs[2]),
            Double.parseDouble(tabs[3]),
            Double.parseDouble(tabs[4])
        );
        performances.add(p);
        fingerprintIndizes.add(index);
      }
    }
    return performances.toArray(new PredictionPerformance[performances.size()]);
  }

  private CovarianceScoring getCovarianceScoring(FingerprintVersion fpVersion, double alpha) throws IOException {
    final HttpGet get;
    try {
      get = new HttpGet(getFingerIdURI("/webapi/covariancetree.csv").build());
    } catch (URISyntaxException e) {
      LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    CovarianceScoring covarianceScoring;
    try (CloseableHttpResponse response = client.execute(get)) {
      if (!isSuccessful(response)) throw new IOException("Cannot get covariance scoring tree information.");
      HttpEntity e = response.getEntity();
      covarianceScoring = CovarianceScoring.readScoring(e.getContent(), ContentType.getOrDefault(e).getCharset(), fpVersion, alpha);
    }
    return covarianceScoring;
  }

  private boolean isSuccessful(CloseableHttpResponse response) {
    return response.getStatusLine().getStatusCode() < 400;
  }



  public List<Scored<FingerprintCandidate>> useThis(Ms2Experiment experiment, IdentificationResult result) throws IOException {
    //TODO: fails on size of version
    CdkFingerprintVersion version = CdkFingerprintVersion.withECFP();



    MaskedFingerprintVersion.Builder maskedBuiled = MaskedFingerprintVersion.buildMaskFor(version);
    maskedBuiled.disableAll();

    final TIntArrayList list = new TIntArrayList(4096);
    int charge = experiment.getPrecursorIonType().getCharge();
    PredictionPerformance[] perf = getStatistics(getType(experiment), list);
    int[] fingerprintIndizes = list.toArray();

    for (int index : fingerprintIndizes) {
      maskedBuiled.enable(index);
    }


    ProbabilityFingerprint print = null;

    MaskedFingerprintVersion maskedVersion = maskedBuiled.toMask();

    FingerblastScoringMethod method = (charge < 0) ? new ScoringMethodFactory.CSIFingerIdScoringMethod(perf) : getCovarianceScoring(maskedVersion, 1d / perf[0].withPseudoCount(0.25).numberOfSamples());
    Fingerblast blast = new Fingerblast(method, null);

    List<FingerprintCandidate> candidates = null;
    try {
      FingerIdJob job = submitJob(experiment, result, maskedVersion);
      print = processFingerIdJob(job);
      candidates = getCandidates(experiment, result);
    } catch (de.unijena.bioinf.chemdb.DatabaseException e) {
      logger.info("Connection with PubChem DB failed.");
      throw new MSDKRuntimeException(e);
    }

    final List<Scored<FingerprintCandidate>> scored = blast.score(candidates, print);
    return scored;
  }

  //TODO: make it better
  private static String fingerIdVersion() {
    return FINGERID_VERSION;
  }

  private static PredictorType getType(Ms2Experiment experiment) {
    int charge = experiment.getPrecursorIonType().getCharge();
    if (charge > 0)
      return PredictorType.CSI_FINGERID_POSITIVE;
    return PredictorType.CSI_FINGERID_NEGATIVE;
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

    final NameValuePair predictor = new BasicNameValuePair("predictors", PredictorType.getBitsAsString(getType(experiment)));

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

  private ProbabilityFingerprint processFingerIdJob(FingerIdJob job) throws URISyntaxException, InterruptedException, TimeoutException, IOException {
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

  private static double[] parseBinaryToDoubles(byte[] bytes) {
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
