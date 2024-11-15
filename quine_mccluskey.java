import java.util.*;



class Quine_McClusky{
	static ArrayList<String> essentialPrimeImplicants = new ArrayList<>();
	static HashSet<String> primeImplicants = new HashSet<>();
	static HashSet<String> uncoveredMinterms = new HashSet<>();
	
	public static void main(String[] args) {
		//Read PLA File
		int numOfInputs = new Scanner(System.in).nextInt();
		
		int numOfOutputs = new Scanner(System.in).nextInt();
		
		int numOfProducts = new Scanner(System.in).nextInt();
		
		
		//Read products
		for(int i = 0; i < numOfProducts; i++) {
			String term = new Scanner(System.in).next();
			int output = new Scanner(System.in).nextInt();
			
			//If product output is 1 then it is a minterm
			if(output == 1)
				uncoveredMinterms.add(term);
		}		
		
		
		//Prime Generation Tabular Method 
		primeGeneration(uncoveredMinterms, numOfInputs);
		
		
		boolean simplification = false;
		do {
			simplification = false;
			//Create Prime Implicant Table
			HashMap<String, HashSet<String>>primeImplicantsTable = createPrimeImplicantsTable(numOfInputs);
			
		
			//Find Essential Prime Implicants
			simplification = findEssentialPrimeImplicants(primeImplicantsTable);
		
			//Row Dominance 
			HashMap<String, HashSet<String>> primeImplicantsTable_rowDominance = createPrimeImplicantsTable(numOfInputs);
			simplification = checkRowDominance(primeImplicantsTable_rowDominance);
		
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
		
		for(String x : essentialPrimeImplicants)
			System.out.println(x);
		
	}
	
	public static void primeGeneration(HashSet<String> minterms, int numOfInputs) {
		boolean simplification = false;
		//Keep combining minterms until there wasn't a simplification
		do {
			simplification = false;
			
			//HashMap Tabular Method (Key = # of 1s and Inner HashMap will store Key = Minterm and Value = checkmark) (Checkmark means if minterm had one bit diff)
			Map<Integer, HashMap<String, Boolean>> table = new HashMap<>();
			
			
			//Iterate through minterms counting the # of 1 they have
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
		String[] primeImplicantsArr = Arrays.copyOf(uncoveredMinterms.toArray(), uncoveredMinterms.size(), String[].class);
		
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