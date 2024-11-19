import java.util.*;



class Quine_McClusky{
	static ArrayList<String> essentialPrimeImplicants = new ArrayList<>();
	static HashSet<String> primeImplicants = new HashSet<>();
	static HashSet<String> uncoveredMinterms = new HashSet<>();
	static HashSet<String> originalMinterms = new HashSet<>();
	static HashSet<String> originalMaxterms = new HashSet<>();
	static HashSet<String> originalDontCares = new HashSet<>();
	
	public static void main(String[] args) {
		//Read PLA File
		Scanner stdin = new Scanner(System.in);
		
		int numOfInputs = -1;
		int numOfOutputs = -1;
		int numOfProducts = -1;
		
		boolean validInput = true;
		
		while(stdin.hasNext()) {
			
			String line = stdin.nextLine().trim();
			
			String[] tokens = line.split("\\s+");
			
			switch(tokens[0]) {
			
				case ".i": numOfInputs = Integer.parseInt(tokens[1]); break;
				
				case ".o": numOfOutputs = Integer.parseInt(tokens[1]); break;
			
				case ".p": {
					numOfProducts = Integer.parseInt(tokens[1]);
				
					//Read Terms
					for(int i = 0; i < numOfProducts; i++) {
						String termLine = stdin.nextLine().trim();
					
						String[] termTokens = termLine.split("\\s+");
					
						String currTerm = termTokens[0];
						String termOutput = termTokens[1];
					
						if(termOutput.equals("1")) {
							uncoveredMinterms.add(currTerm);
							originalMinterms.add(currTerm);
						}
						else if(termOutput.equals("-"))
							originalDontCares.add(currTerm);
						else if(termOutput.equals("0"))
							originalMaxterms.add(currTerm);
					}
				
					break;
				
				}
			
				case ".e": validInput = true; break;
			
				default: validInput = false; 
			
			}	
		}
		
		
		if(!validInput)
			System.out.println("Invalid PLA Format Input");
		
		else {
			//Prime Generation Tabular Method 
			primeGeneration(originalMinterms, originalDontCares, numOfInputs);
		
			//Keep doing QM method until no simplifcation occurs 
			boolean simplification = false;
			do {
				simplification = false;
				//Create Prime Implicant Table
				HashMap<String, HashSet<String>>primeImplicantsTable = createPrimeImplicantsTable(numOfInputs);
			
		
				//Find Essential Prime Implicants
				simplification = findEssentialPrimeImplicants(primeImplicantsTable);
				
				if(uncoveredMinterms.size() == 0)
					break;
		
				//Row Dominance 
				HashMap<String, HashSet<String>> primeImplicantsTable_rowDominance = createPrimeImplicantsTable(numOfInputs);
				simplification = checkRowDominance(primeImplicantsTable_rowDominance);
				
				if(uncoveredMinterms.size() == 0)
					break;
		
				//Column Dominance 
				HashMap<String, HashSet<String>> primeImplicantsTable_columnDominance = createPrimeImplicantsTable(numOfInputs);
				simplification = checkColumnDominance(primeImplicantsTable_columnDominance);
			}while(simplification);
		
			//If minterms are still uncovered then choose whichever prime implicant that covers them and add them to essentialPrimeImplicant
			HashMap<String, HashSet<String>>primeImplicantsTable = createPrimeImplicantsTable(numOfInputs);
		
			//Iterate through all remaining uncovered minterms and choose first prime implicant that covers them
			Iterator<String> finalUncoveredMinterms = uncoveredMinterms.iterator();
			while(finalUncoveredMinterms.hasNext()) {
			
				String currMinterm = finalUncoveredMinterms.next();
			
				Iterator<String> currMintermPrimeImplicants = primeImplicantsTable.get(currMinterm).iterator();
				boolean found = false;
			
				while(currMintermPrimeImplicants.hasNext() && !found) {
					String currPrimeImplicant = currMintermPrimeImplicants.next();
				
					if(!essentialPrimeImplicants.contains(currPrimeImplicant)) {
						found = true;
						essentialPrimeImplicants.add(currPrimeImplicant);
					}
				}
			}
		
			//Ouput PLA Format
			
			System.out.println(".i " + numOfInputs);
			System.out.println(".o " + numOfOutputs);
			numOfProducts = essentialPrimeImplicants.size() + originalDontCares.size() + originalMaxterms.size();
			System.out.println(".p " + numOfProducts);
			
			for(String x : essentialPrimeImplicants)
				System.out.println(x + " 1");
			for(String y : originalDontCares)
				System.out.println(y + " -");
			for(String z : originalMaxterms)
				System.out.println(z + " 0");
			
			System.out.print(".e");
		}
		
	}
	
	public static void primeGeneration(HashSet<String> minterms, HashSet<String> dontCares, int numOfInputs) {
		boolean simplification = false;
		boolean firstStage = true;
		//Keep combining minterms until there wasn't a simplification
		do {
			simplification = false;
			
			//HashMap Tabular Method (Key = # of 1s and Inner HashMap will store Key = Minterm/Don't Cares and Value = checkmark)
			//Checkmark means that minterm/don't care had one bit diff 
			Map<Integer, HashMap<String, Boolean>> table = new HashMap<>();
			
			
			//Iterate through minterms counting the # of 1s they have
			Iterator<String> mintermList = minterms.iterator();
			
			while(mintermList.hasNext()) {
				String currMinterm = mintermList.next();
				int oneCounter = 0;
				
				for(int j = 0; j < currMinterm.length(); j++) {
					char x = currMinterm.charAt(j);
					
					if(x == '1')
						oneCounter++;
				}
				
				//Insert into HashMap Tabular Method 
				if(!table.containsKey(oneCounter))
					table.put(oneCounter, new HashMap<>());
				
				
				table.get(oneCounter).put(currMinterm, false);
			}
			
			//Iterate through don't cares counting the # of 1s they have (Only for First Stage)
			if(firstStage) {
				Iterator<String> dontCaresList = dontCares.iterator();
			
				while(dontCaresList.hasNext()) {
					String currDontCare = dontCaresList.next();
					int oneCounter = 0;
					
					for(int x = 0; x < currDontCare.length(); x++) {
						char curr = currDontCare.charAt(x);
						
						if(curr == '1')
							oneCounter++;
					}
					
					if(!table.containsKey(oneCounter)) 
						table.put(oneCounter, new HashMap<>());
					
					table.get(oneCounter).put(currDontCare, false);
				}
				
				firstStage = false;
			}
		
			
			//Reset minterms
			minterms = new HashSet<>();
			
			if(combineMinterms(minterms, numOfInputs, table))
				simplification = true;
			
			//Check if any minterms WERE NOT combined
			Iterator<Integer> iter = table.keySet().iterator();
			
			while(iter.hasNext()) {
				int currIter = iter.next();
				Iterator<String> secondIter = table.get(currIter).keySet().iterator();
				
				//If minterm wasn't combined put them in Prime Implicant Table
				while(secondIter.hasNext()) {
					String currMinterm = secondIter.next();					
					if(table.get(currIter).get(currMinterm) == false)
						primeImplicants.add(currMinterm);
					
				}
			}
		
		}while(simplification);
		
	}
	
	public static boolean combineMinterms(HashSet<String> minterms, int numOfInputs, Map<Integer, HashMap<String, Boolean>> table) {
		//Return if simplifcation existed 
		boolean simplification = false;
		
		int leftCompare = 0;
		int rightCompare = 1;
		
		while(rightCompare <= numOfInputs) {
			
			//Checking if minterms exist with leftCompare # of 1s and rightCompare # of 1s
			if(!table.containsKey(leftCompare) || !table.containsKey(rightCompare)) {
				leftCompare++;
				rightCompare++;
				continue;
			}
			
		
			Iterator<String> left = table.get(leftCompare).keySet().iterator();
			
			while(left.hasNext()) {
				Iterator<String> right = table.get(rightCompare).keySet().iterator();
				
				String currLeft = left.next();
				
				while(right.hasNext()) {
					String currRight = right.next();
					
					int bitDifferenceCounter = 0;
					int index = 0;
					
					//Check for one bit difference
					for(int i = 0; i < numOfInputs; i++) {
						if(bitDifferenceCounter > 1) {
							break;
						}
						
						char x = currLeft.charAt(i);
						char y = currRight.charAt(i);
						
						if(x != y) {
							bitDifferenceCounter++;
							index = i;
						}
					}
					
					//If one bit difference add new simplified minterm
					if(bitDifferenceCounter == 1) {
						char[] temp = currLeft.toCharArray();
						
						temp[index] = '-';
						
						minterms.add(String.copyValueOf(temp));
						
						simplification = true;
						
						//Checkmark minterms that were combined
						table.get(leftCompare).put(currLeft, true);
						table.get(rightCompare).put(currRight, true);
					}
					
				}
				
			}
			
			rightCompare++;
			leftCompare++;
		}
	
		return simplification;					
	}
	
	public static HashMap<String, HashSet<String>> createPrimeImplicantsTable(int numOfInputs) {
		
		//HashMap mintermList (Key - Minterm Value - HashSet) 
		//HashSet - Stores Prime Implicant Covers 
		HashMap<String, HashSet<String>> primeImplicantTable = new HashMap<>();
		
		//Fill primeImplicantTable with Keys 
		Iterator<String> mintermKeys = uncoveredMinterms.iterator();
		
		
		while(mintermKeys.hasNext())
			primeImplicantTable.put(mintermKeys.next(), new HashSet<>());
			
		
		//Iterate through every Prime Implicant and compare with every minterm to see if it covers
		Iterator<String> primeImplicantIter = primeImplicants.iterator();
	
		
		//Create Prime Implicant Table
		while(primeImplicantIter.hasNext()) {
			String currPrimeImplicant = primeImplicantIter.next();
			
			if(essentialPrimeImplicants.contains(currPrimeImplicant))
				continue;
			
			Iterator<String> mintermsIter = uncoveredMinterms.iterator();
			
			while(mintermsIter.hasNext()) {
				String currMinterm = mintermsIter.next();
				
				boolean validCover = true;
				
				for(int i = 0; i < numOfInputs; i++) {
					char x = currPrimeImplicant.charAt(i);
					char y = currMinterm.charAt(i);
					
					
					if(x == '-')
						continue;
					
					if(x != y) {
						validCover = false;
						break;
					}
					
				}
				
				if(validCover)
					primeImplicantTable.get(currMinterm).add(currPrimeImplicant);
				
			}
			
		}
		
		return primeImplicantTable;			
	}
	
	public static boolean findEssentialPrimeImplicants (HashMap<String, HashSet<String>> primeImplicantTable) {
		String[] mintermArr = Arrays.copyOf(uncoveredMinterms.toArray(), uncoveredMinterms.size(), String[].class);
		
		boolean essentialPrimeImplicantFound = false;
		
		//For every minterm check if it has one prime implicant cover
		for(int i = 0; i < mintermArr.length; i++) {
			String currMinterm = mintermArr[i];
			
			if(!uncoveredMinterms.contains(currMinterm))
				continue;
			
			int numOfCovers = primeImplicantTable.get(currMinterm).size();
			
			//If minterm has one prime implicant cover check if all the minterms this ESSENTIAL PRIME IMPLICANT covers 
			if(numOfCovers == 1) {
				Iterator<String> getEssentialPrimeImplicant = primeImplicantTable.get(currMinterm).iterator();
				
				String currEssentialPrimeImplicant = getEssentialPrimeImplicant.next();
				essentialPrimeImplicants.add(currEssentialPrimeImplicant);
				uncoveredMinterms.remove(currMinterm);
				
				for(int j = i + 1; j < mintermArr.length; j++) {
					Iterator<String> mintermIter = primeImplicantTable.keySet().iterator();
					
					while(mintermIter.hasNext()) {
						String tempMinterm = mintermIter.next();
						
						if(primeImplicantTable.get(tempMinterm).contains(currEssentialPrimeImplicant))
							uncoveredMinterms.remove(tempMinterm);		
					}	
				}
				
				essentialPrimeImplicantFound = true;
			}
			
		}
		
		return essentialPrimeImplicantFound;
	}
	
	public static boolean checkRowDominance(HashMap<String, HashSet<String>> primeImplicantsTable) {
		String[] mintermArr = Arrays.copyOf(uncoveredMinterms.toArray(), uncoveredMinterms.size(), String[].class);
		
		boolean rowDominanceFound = false;
		
		//Compare a minterm's row with every other minterm's row
		for(int i = 0; i < mintermArr.length; i++) {
			String currMinterm = mintermArr[i];
			
			if(!uncoveredMinterms.contains(currMinterm))
				continue;
			
			Iterator<String> rowA = primeImplicantsTable.get(currMinterm).iterator();
			
			boolean validRowDominance = true;
			String potentialEssentialImplicant = "";
			
			//If all prime implicants in row A are present in row B then row dominance 
			for(int j = i + 1; j < mintermArr.length; j++) {
				String rowB_Minterm = mintermArr[j];
				if(!uncoveredMinterms.contains(rowB_Minterm))
					continue;
				
				while(rowA.hasNext()) {
					potentialEssentialImplicant = rowA.next();
					
					if(!primeImplicantsTable.get(rowB_Minterm).contains(potentialEssentialImplicant)) {
						validRowDominance = false;
						break;
					}
				}
				
				//If all prime implicants in row A are present in Row B
				if(validRowDominance) {
					//Mark row A minterm as covered 
					uncoveredMinterms.remove(currMinterm);
					rowDominanceFound = true;
				}		
			}		
		}
		
		return rowDominanceFound;
	}
	
	public static boolean checkColumnDominance(HashMap<String, HashSet<String>> primeImplicantsTable) {
		//For every prime implicant (IF NOT ESSENTIAL ALREADY) check how many minterms it covers
		String[] primeImplicantsArr = Arrays.copyOf(primeImplicants.toArray(), primeImplicants.size(), String[].class);
		
		boolean columnDominanceFound = false;
		
		//Prime implicant that covers the most is essential (Index - # of Minterms covered Element - ArrayList)
		//ArrayList stores all prime implicants that have index # of minterms covered
		ArrayList<String>[] frequency = new ArrayList[uncoveredMinterms.size() + 1];
		for(int i = 0; i < frequency.length; i++) 
			frequency[i] = new ArrayList<>();
		
		
		for(int i = 0; i < primeImplicantsArr.length; i++) {
			String currPrimeImplicant = primeImplicantsArr[i];
			int indexFreq = 0;
			
			//Check number of minterms currPrimeImplicant covers
			Iterator<String> mintermList = uncoveredMinterms.iterator();
			
			while(mintermList.hasNext()) {
				String currMinterm = mintermList.next();
				
				if(primeImplicantsTable.get(currMinterm).contains(currPrimeImplicant))
					indexFreq++;
			}
			
			frequency[indexFreq].add(currPrimeImplicant);
		}
		
		for(int i = frequency.length - 1; i >= 0; i--) {
			
			if(frequency[i].size() == 1) {
				
				//Store essential prime implicant 
				essentialPrimeImplicants.add(frequency[i].get(0));
				
				//Checkmark all minterms that this essential prime implicant covers
				Iterator<String> mintermIter = uncoveredMinterms.iterator();
				while(mintermIter.hasNext()) {
					
					String currMinterm = mintermIter.next();
					
					if(primeImplicantsTable.get(currMinterm).contains(frequency[i].get(0)))
						uncoveredMinterms.remove(currMinterm);
				}
				
				columnDominanceFound = true;
			}
			
			else if(frequency[i].size() > 1)
				break;
		}
		
		
		return columnDominanceFound;
	}
	
}