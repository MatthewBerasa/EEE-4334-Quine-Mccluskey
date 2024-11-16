# Quine Mccluskey 
This project implements the Quine-McCluskey algorithm in Java for EEE 4334. It allows the simplification of two-level boolean functions.  

## 📖 Usage
### ⚙️ General
  1. Ensure the PLA format input file is in the same directory as the `quine_mccluskey.java` file 
  2. Open terminal from the project folder
  3. Run command:
      - `java quine_mccluskey.java <inputFileName.PLA` (Display output in terminal)
      - `java quine_mccluskey.java <inputFileName.PLA >outputFileName.PLA` (Create output file in directory)  
  4. The program will read the input PLA file and output the result

### 📄 Input and Output files

#### Input File

The input file must be in PLA Format. 

Specifications:
  - One Output
  - Max Boolean Space of 6
  - Must list out all input combinations for products (fully specified)
  - Each input combination must specifiy output:
      - 0
      - 1
      - \- (Don't Care) 

Example PLA Format Input: 
```pla
.i 4
.o 1
.p 16
0000 1
0001 0
0010 1
0011 0
0100 0
0101 1
0110 1
0111 1
1000 1
1001 0
1010 1
1011 0
1100 1
1101 1
1110 1
1111 1
.e
```

#### Output File

The output file will be in PLA Format. The output will display the maxterms, don't cares, and the simplified minterms.

Example PLA Format Output:
```pla
.i 4
.o 1
.p 9
-1-1 1
-0-0 1
-11- 1
11-- 1
0011 0
0001 0
0100 0
1001 0
1011 0
.e
```
