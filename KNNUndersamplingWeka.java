package weka.filters.supervised.instance;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;
import weka.filters.SimpleBatchFilter;
import weka.filters.SupervisedFilter;


public class KNNUndersamplingWeka extends SimpleBatchFilter implements SupervisedFilter,
		OptionHandler, TechnicalInformationHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2103039882958523000L;

	/** Number of references */

	private int k = 5;

	private int t_threshold = 1;
	
	
	protected int w_majorityClass = 1;

	
	public static Instances original;
	
	/** for nearest-neighbor search. */
	protected NearestNeighbourSearch K_NNSearch = new LinearNNSearch();
	
	private Random random;
	
	// inner class to hold a pair of doubles
	// (for maintaining a running list of nearest neighbors)
	class ClassDistance {
		double _class;

		double distance;

		int index;

	}

	
	public KNNUndersampling() {

	}

	/**
	 * Returns the description of the classifier.
	 * 
	 * @return description of the KNN class.
	 */
	public String globalInfo() {
		return "Implementation of SMOTEFilter (Syntethic Minority Over-sampling Technique), "
				+ "aplied over a KNN algorithm, in order to improve the classification "
				+ "with imbalanced datasets.\n\n"

				+ "For more information see:\n\n"
				+ getTechnicalInformation().toString();

	}

	/**
	 * sets k
	 * 
	 * @param new_k
	 *            new k value.
	 */
	public void setK(int new_k) {
		k = new_k;
	}

	/**
	 * gets k
	 * 
	 * @return k
	 */
	public int getK() {
		return k;
	}

	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {

		return inputFormat;
	}

	protected Instances process(Instances data) throws Exception {

		long startT= System.currentTimeMillis();
		
		original = new Instances(data);
		//Initialize random seed
		
		
		List <Integer> toBeRemoved = obtainInstancesToRemove(data);
		
		
		Instances cleanData = new Instances(data,0);
		for (int i=0;i<data.numInstances();i++) {
			
			if (!toBeRemoved.contains(i)) {
				Instance instance = (Instance) data.instance(i);
				cleanData.add(instance);
			}
		} 

		final long elapsed = System.currentTimeMillis()-startT;
		
		new Thread() {
			
			public void run()
			{
				JOptionPane.showConfirmDialog(null, "Elapsed time:" + elapsed);
				
			}
			
		}.start();
		
		
		
		return cleanData;

	}

	protected List <Integer> obtainInstancesToRemove(Instances data) {

		// Obtain the samples from class w
		Instances majority = new Instances(data, 0);
		Enumeration en = data.enumerateInstances();
		while (en.hasMoreElements()) {
			Instance instance = (Instance) en.nextElement();
			if (instance.classValue() == w_majorityClass) {
				majority.add(instance);
			}
		}
		int T = majority.numInstances();

		


		// Instances for synthetic samples
		List <Integer> toRemove = new ArrayList();

		/*
		 * Compute k nearest neighbors for i, and save the indices in the
		 * nnarray
		 */
		
		try {
			K_NNSearch.setInstances(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		en = data.enumerateInstances();
		int i=0;
		while (en.hasMoreElements()) {
			Instance instance = (Instance) en.nextElement();
			if (instance.classValue() == w_majorityClass) {
				List <Instance>knnList = generateKnnList(instance);	
				if (decideToRemove( knnList)) {
						toRemove.add(i);
				}
			}
			i++;
		}
		
		return toRemove;
	}

	/* Function to take a decision about remove or not the instance */
	protected boolean decideToRemove( List<Instance> knnList) {
		
		int numberFromMinorityClasses=0;
		
		for (int j=0;j<knnList.size();++j)
		{
			Instance neighbor = knnList.get(j);
			if (neighbor.classValue() != w_majorityClass) {
				numberFromMinorityClasses++;
			}
			
			
		}
		//TODO HOW TO DECIDE IF NOT ALL NEIGHBORS ARE FROM MINORITY?
		return  (numberFromMinorityClasses>=t_threshold);
			

	}

	protected List<Instance> generateKnnList(Instance instance) {
		List knnList = new ArrayList();
		try {
			
			Instances nns = K_NNSearch.kNearestNeighbours(instance, this.k);
			
			
			
			for (int i = 0; i < nns.numInstances(); i++) {
				knnList.add(nns.instance(i));
			}
			return knnList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		

		
	}

	// ----------------------------------------------------------------------------

	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		// result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// other
		// result.enable(Capability.ONLY_MULTIINSTANCE);

		return result;
	}

	

	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result
				.setValue(
						Field.AUTHOR,
						"Nitesh V. Chawla, Kevin W. Bowyer, Lawrence O. Hall, W. Philip Kegelmeyer."
								+ "\nImplemented in Weka by Marcelo Beckmann - Federal University of Rio de Janeiro - COPPE/PEC");
		result.setValue(Field.TITLE,
				"\nSMOTE: Synthetic Minority Over-sampling TEchnique");
		result.setValue(Field.BOOKTITLE,
				"Journal of Artificial Inteligence Research 16");
		result.setValue(Field.EDITOR,
				"AI Access Foundation and Morgan Kaufmann");
		result.setValue(Field.YEAR, "2002");
		result.setValue(Field.PAGES, "321-357");

		return result;
	}

	public String percentToUndersampleTipText() {

		return "Percent of instances to be undersampled.";
	}

	public String kTipText() {
		return "Number of Nearest Neighbors.";
	}

	public String amountOfSMOTETipText() {
		return "Amount of SMOTEFilter N% to be created. Use multiples of 100.";
	}

	public String minorityClassTipText() {
		return "Index of minority class, starting with 0.";
	}

	public Enumeration listOptions() {
		Vector result = new Vector();

		result.addElement(new Option(
				"\tNumber of Nearest Neighbors (default 2).", "K", 0,
				"-K <number of references>"));

		result.addElement(new Option(
						"\tThreshold decision to remove , based in the count of neighbors belonging to another class (default 1).",
						"t", 0,
						"-t <Threshold decision>"));

		result.addElement(new Option(
				"\tIndex of minority class, starting with 0 (default 0).", "w",
				0, "-w <Index of minority class>"));
		
		result.addElement(new Option("\tSeed number used to generate random numbers. If -1 uses the current time in milliseconds^2.", "m", -1,
				"-m <Random seed>"));
		

		return result.elements();
	}

	public void setOptions(String[] options) throws Exception {
		// setDebug(Utils.getFlag('D', options));

		String option = Utils.getOption('k', options);
		if (option.length() != 0)
			k = Integer.parseInt(option);
		else
			k = 5;

	

		option = Utils.getOption('w', options);
		if (option.length() != 0)
			w_majorityClass = Integer.parseInt(option);
		else
			w_majorityClass = 0;
		
		
	

		
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return the list of current option settings as an array of strings
	 */
	public String[] getOptions() {
		Vector result;

		result = new Vector();

		// if (getDebug())
		// result.add("-D");
		

		result.add("-k");
		result.add("" + k);

		result.add("-t");
		result.add("" + t_threshold);

		
		result.add("-w");
		result.add("" + w_majorityClass);

	

		
		return (String[]) result.toArray(new String[result.size()]);
	}

	

	public int getMajorityClass() {
		return w_majorityClass;
	}

	public void setMajorityClass(int w) {
		this.w_majorityClass = w;
	}

	
	
	
	/**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            should contain arguments to the filter: use -h for help
	 */
	public static void main(String[] argv) {
		runFilter(new SMOTEFilter(), argv);
	}

	public String getRevision() {
		// TODO Auto-generated method stub
		return "No revision";
	}
	
	public NearestNeighbourSearch getNNSearch() {
		return K_NNSearch;
	}

	public void setNNSearch(NearestNeighbourSearch search) {
		K_NNSearch = search;
	}

	
	public int getThreshold() {
		return t_threshold;
	}

	public void setThreshold(int t_threshold) {
		this.t_threshold = t_threshold;
	}

	
}
