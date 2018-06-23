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
import de.unijena.bioinf.chemdb.DBLink;
import de.unijena.bioinf.chemdb.FingerprintCandidate;
import de.unijena.bioinf.chemdb.RESTDatabase;
import de.unijena.bioinf.chemdb.SearchStructureByFormula;
import de.unijena.bioinf.fingerid.blast.CovarianceScoring;
import de.unijena.bioinf.fingerid.blast.Fingerblast;
import de.unijena.bioinf.fingerid.blast.FingerblastScoringMethod;
import de.unijena.bioinf.fingerid.blast.ScoringMethodFactory;
import de.unijena.bioinf.fingerid.predictor_types.PredictorType;
import de.unijena.bioinf.utils.systemInfo.SystemInformation;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.IonAnnotation;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
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

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FingerIdWebMethod implements MSDKMethod<List<IonAnnotation>> {

  private final static SmilesParser smp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
  private final static CloseableHttpClient client = HttpClients.createSystem(); // Threadsafe
  private final static Logger logger = LoggerFactory.getLogger(FingerIdWebMethod.class);
  private final static BasicNameValuePair UID = new BasicNameValuePair("uid", SystemInformation.generateSystemKey());
  private final static String FINGERID_SOURCE = "https://www.csi-fingerid.uni-jena.de";
  private final static String FINGERID_VERSION = "1.1.2";
  private final static SearchStructureByFormula searchDB = new RESTDatabase(BioFilter.ALL);
  private final static Gson gson = new Gson();

  private final Ms2Experiment experiment;
  private final SiriusIonAnnotation ionAnnotation;
  private final PredictionPerformance[] perf;
  private final MaskedFingerprintVersion version;
  private final Fingerblast blaster;
  private final int candidatesAmount;
  private boolean cancelled;
  private List<IonAnnotation> newAnnotations;
  private int finishedItems;


  public FingerIdWebMethod(Ms2Experiment experiment, IonAnnotation ionAnnotation, int candidatesAmount) throws MSDKException {
    this.experiment = experiment;
    if (ionAnnotation instanceof SiriusIonAnnotation)
      this.ionAnnotation = (SiriusIonAnnotation) ionAnnotation;
    else
      throw new MSDKException("Provided IonAnnotation is not from Sirius module");

    try {
      final TIntArrayList list = new TIntArrayList(4096);
      perf = getStatistics(getType(), list);
      version = buildFingerprintVersion(list);
      blaster = createBlaster(perf);
    } catch (IOException e) {
      throw new MSDKException(e);
    }

    newAnnotations = new LinkedList<>();
    this.candidatesAmount = candidatesAmount;
    finishedItems = 0;
  }

  private static String fingerIdVersion() {
    return FINGERID_VERSION;
  }

  private URIBuilder getFingerIdURI(String path) throws URISyntaxException {
    if (path == null)
      path = "";
    URIBuilder builder = new URIBuilder(FINGERID_SOURCE);
    builder.setPath("/csi-fingerid-" + FingerIdWebMethod.fingerIdVersion() + path);

    return builder;
  }

  private List<FingerprintCandidate> getCandidates() throws de.unijena.bioinf.chemdb.DatabaseException {
    PrecursorIonType ionType = experiment.getPrecursorIonType();
    IMolecularFormula iFormula = ionAnnotation.getFormula();
    MolecularFormula formula = MolecularFormula.parse(MolecularFormulaManipulator.getString(iFormula));



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
      String line;
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



  private List<Scored<FingerprintCandidate>> processSiriusAnnotation() throws MSDKException, MSDKRuntimeException {
    ProbabilityFingerprint print;
    List<Scored<FingerprintCandidate>> scored;

    List<FingerprintCandidate> candidates;
    try {
      FingerIdJob job = submitJob(); //TODO: There is need of FTree, create new SimpleIonAnnotation
      print = processFingerIdJob(job);
      candidates = getCandidates();
      scored = blaster.score(candidates, print);
    } catch (de.unijena.bioinf.chemdb.DatabaseException e) {
      logger.error("Connection with PubChem DB failed.");
      throw new MSDKRuntimeException(e);
    } catch (URISyntaxException t) {
      logger.error("Failed to construct URI");
      throw new MSDKException(t);
    } catch (IOException f) {
      throw new MSDKException(f);
    } catch (TimeoutException time) {
      logger.error("Timeout on job status update has expired!");
      throw new MSDKRuntimeException(time);
    } catch (InterruptedException i) {
      throw new MSDKRuntimeException(i);
    }

    return scored;
  }

  private MaskedFingerprintVersion buildFingerprintVersion(final TIntArrayList predictionIndizes) throws IOException {
    CdkFingerprintVersion version = CdkFingerprintVersion.withECFP();
    MaskedFingerprintVersion.Builder maskedBuiled = MaskedFingerprintVersion.buildMaskFor(version);
    maskedBuiled.disableAll();

    int[] indizes = predictionIndizes.toArray();
    for (int index : indizes) {
      maskedBuiled.enable(index);
    }

    return maskedBuiled.toMask();
  }

  private Fingerblast createBlaster(PredictionPerformance[] perf) throws IOException {
    FingerblastScoringMethod method = null;
    PredictorType type = getType();
    if (type == PredictorType.CSI_FINGERID_NEGATIVE)
      method = new ScoringMethodFactory.CSIFingerIdScoringMethod(perf);
    else
      method = getCovarianceScoring(version, 1d / perf[0].withPseudoCount(0.25).numberOfSamples());

    return new Fingerblast(method, null);
  }


  private PredictorType getType() {
    int charge = this.experiment.getPrecursorIonType().getCharge();
    if (charge > 0)
      return PredictorType.CSI_FINGERID_POSITIVE;
    return PredictorType.CSI_FINGERID_NEGATIVE;
  }

  public FingerIdJob submitJob() throws IOException, URISyntaxException{
    final HttpPost post = new HttpPost(getFingerIdURI("/webapi/predict.json").build());
    final FTree ftree = ionAnnotation.getFTree();

    post.setEntity(buildParams(ftree));

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

  private UrlEncodedFormEntity buildParams(FTree ftree) throws IOException {
    final String stringMs = getExperimentAsString();
    final String jsonTree = getTreeAsString(ftree);

    final NameValuePair ms = new BasicNameValuePair("ms", stringMs);
    final NameValuePair tree = new BasicNameValuePair("ft", jsonTree);
    final NameValuePair predictor = new BasicNameValuePair("predictors", PredictorType.getBitsAsString(getType()));

    final UrlEncodedFormEntity params = new UrlEncodedFormEntity(Arrays.asList(ms, tree, predictor, UID));
    return params;
  }

  private String getTreeAsString(FTree ftree) throws IOException {
    final FTJsonWriter writer = new FTJsonWriter();
    final StringWriter sw = new StringWriter();
    writer.writeTree(sw, ftree);
    return sw.toString();
  }

  private String getExperimentAsString() throws  IOException {
    final JenaMsWriter writer = new JenaMsWriter();
    final StringWriter sw = new StringWriter();
    try (final BufferedWriter bw = new BufferedWriter(sw)) {
      writer.write(bw, experiment);
    }
    return sw.toString();
  }

  private ProbabilityFingerprint processFingerIdJob(FingerIdJob job) throws URISyntaxException, InterruptedException, TimeoutException, IOException {
    // RECEIVE RESULTS
    new HttpGet(getFingerIdURI("/webapi/job.json")
        .setParameter("jobId", String.valueOf(job.jobId))
        .setParameter("securityToken", job.securityToken)
        .build());
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

  private boolean updateJobStatus(FingerIdJob job) throws URISyntaxException {
    final HttpGet get = new HttpGet(getFingerIdURI("/webapi/job.json")
        .setParameter("jobId", String.valueOf(job.jobId))
        .setParameter("securityToken", job.securityToken)
        .build());
    try (CloseableHttpResponse response = client.execute(get)) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ContentType.getOrDefault(response.getEntity()).getCharset()));
      GetResponse getResponse = gson.fromJson(reader, GetResponse.class);
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
    } catch (Throwable t) {
      logger.error("Error when updating job #" + job.jobId, t);
      throw new MSDKRuntimeException(t);
    }
    return false;
  }

  private double[] parseBinaryToDoubles(byte[] bytes) {
    final TDoubleArrayList data = new TDoubleArrayList(2000);
    final ByteBuffer buf = ByteBuffer.wrap(bytes);
    buf.order(ByteOrder.LITTLE_ENDIAN);
    while (buf.position() < buf.limit()) {
      data.add(buf.getDouble());
    }
    return data.toArray();
  }

  @Nullable
  @Override
  public Float getFinishedPercentage() {
    return 1f * finishedItems / candidatesAmount;
  }

  @Nullable
  @Override
  public List<IonAnnotation> execute() throws MSDKException {
    List<Scored<FingerprintCandidate>> candidates = processSiriusAnnotation();

    for (Scored<FingerprintCandidate> scoredCandidate: candidates) {
      if (cancelled)
        return null;
      final SiriusIonAnnotation extendedAnnotation = new SiriusIonAnnotation(ionAnnotation);
      final FingerprintCandidate candidate = scoredCandidate.getCandidate();


      DBLink[] links = candidate.getLinks();
      if (links != null && links.length > 0) {
        String id = links[0].id;
        System.out.println("What?"); //todo: make magic here
//        extendedAnnotation.setAccessionURL();
      }

      synchronized (smp) {
        try {
          String smilesString = candidate.getSmiles();
          IAtomContainer container = smp.parseSmiles(smilesString);
          extendedAnnotation.setChemicalStructure(container);
          extendedAnnotation.setSMILES(smilesString);
        } catch(org.openscience.cdk.exception.InvalidSmilesException e){
          logger.error("Incorrect SMILES string");
          throw new MSDKException(e);
        }
      }
      extendedAnnotation.setInchiKey(candidate.getInchiKey2D());
      extendedAnnotation.setDatabase("Pubchem"); //TODO: not sure
      extendedAnnotation.setDescription(candidate.getName());

      newAnnotations.add(extendedAnnotation);
      finishedItems++;
      if (finishedItems == candidatesAmount)
        break;
    }


    return newAnnotations;
  }

  @Nullable
  @Override
  public List<IonAnnotation> getResult() {
    return newAnnotations;
  }

  @Override
  public void cancel() { //TODO: make it
    cancelled = true;
  }


  class GetResponse {
    public String securityToken;
    public int jobId;
    byte[] plattBytes;
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

