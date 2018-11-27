library(FNN)

#
# KNN-Undersampling algorithm
#
# Description
# 
# Performs an undersampling removal of instances from majority class, according the KNN-Und method.
# 
# Usage
#
# knn_und(X,y, majority_class, k=7, t=3,algorithm=c("kd_tree", "cover_tree", "CR", "brute"))
#
# Arguments
#
# X
# an input data matrix.
#
# y
# the label data array
#
# majority_class
# the value that identifies the majority class label in the y array
#
# k
# the maximum number of nearest neighbors to search. The default value is set to 10.
#
# t
# A threshold of nearest neighbors belonging to the minority class.
# The examples from majority class with a number of minority class neighbors >=t must be removed.
# The default value is set to 5.
#
# algorithm	
# the nearest neighbor searching algorithm. The default is set to "kd_tree"
#
# Return Value
# 
# Returns an array with TRUE/FALSE values. 
# The TRUE values represent the examples that must remain in the X,y inputs, according the KNN-Und heuristic.
# The FALSE valus represent the examples that must be removed in he X,y inputs, according the KNN-Und heuristic.
# 
#
# Author(s)
# 
# Marcelo Beckmann, Nelson F.F. Ebecken, Beatriz de Lima. To report any bugs or suggestions please email: beckmann.marcelo@gmai.com
# 
# References
#
# Beckmann, M., Ebecken, N. & De Lima, B., 2015. A KNN Undersampling Approach for Data Balancing. JILSA - Journal of Intelligent Learning Systems and Applications, pp. 7, 104-116.
#
# Example:
# 
#
# pima = read.csv("pima-indians-diabetes_normalize.csv")
#
# nrows=nrow(pima)
# ncols=ncol(pima)
#
# X=pima[1:nrows,1:(ncols-1)]
# y=pima[1:nrows,ncols]
#
#
# result = knn_und(X,y,majority_class=0,k=7,t=5)
# pima_undersampled = pima[result,1:ncols]
#
# str(pima_undersampled)
#
#
#

knn_und <- function(X,y,majority_class, k=7, t=5, algorithm="kd_tree"){
  nrows=nrow(X)
  ncols=ncol(X)
  result= array(FALSE, dim=nrows)
  
  for (i in 1:nrows){
    example=X[i,1:ncols]
    current_label=y[i]
    
    if (current_label != majority_class) {
      result[i]=TRUE
      next
    }
  
    nns_indexes=knnx.index(X,example,k=k,algorithm="kd_tree")
    nns_labels=y[nns_indexes]
    num_of_minority=0
    
    for (nn_label in nns_labels)
    {
     
      if (nn_label!=majority_class)
      {
          num_of_minority=num_of_minority+1
      }
    }
    if (num_of_minority>t)
    {

        result[i]=FALSE
    }
    else {
    
      result[i]=TRUE
    }

  }

  return(result)
}


pima = read.csv("C:/Users/beckmann/Dropbox/CPC802_cont_2014/projetos/smote/data_geapa/pima-indians-diabetes_normalize.csv")

nrows=nrow(pima)
ncols=ncol(pima)

X=pima[1:nrows,1:(ncols-1)]
y=pima[1:nrows,ncols]


result = knn_und(X,y,majority_class=0,k=7,t=5)
pima_undersampled = pima[result,1:ncols]

str(pima_undersampled)

