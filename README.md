# arff-to-csv
A simple ARFF to CSV conversion tool. There are many rich datasets that are publicly available, but are in Weka's ARFF format. Some applications may require CSV, so this tool makes it easy to convert between ARFF to CSV. 

# Requirements
Java 1.8

# Usage
Using the executable jar, run the following: 
  
  ```java -jar arfftocsv.jar -i [PATH_TO_ARFF] -o [PATH_TO_OUTPUT_CSV] [OPTIONS]```

Options currently supported are shown below:

|Option| Default Value| Description|
|------|--------------|------------|
|`-header`|FALSE| Include a header row |

# Future Features
- The development for this tool was originally motivated by the need for multilabel classification CSV datasets, where feature/attributes with multilabels are pipe separated values (PSV). The ability to "collapse" n-contiguous columns into 1 PSV column is currently a work-in-progress. 
