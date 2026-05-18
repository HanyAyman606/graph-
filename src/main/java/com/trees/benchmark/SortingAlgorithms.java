package com.trees.benchmark;

class BubbleSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "BubbleSort";}
@Override public void sort(int[] arr){
 int n=arr.length;
  boolean sort_complete_flag=false;
 for(int i=0;i<n-1;i++){
  boolean swapped=false;
   for(int j=0;j<n-i-1;j++){
    if(arr[j]>arr[j+1]){
     int t=arr[j];arr[j]=arr[j+1];arr[j+1]=t;
     swapped=true;
    }
   }
  if(!swapped){
  sort_complete_flag=true;
  break;
  }
 }
}
}

class InsertionSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "InsertionSort";}
@Override public void sort(int[] arr){
 for(int i=1;i<arr.length;i++){
  int key=arr[i];
  int j=i-1;
   while(j>=0&&arr[j]>key){arr[j+1]=arr[j];j--;}
  arr[j+1]=key;
 }
}
}

class SelectionSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "SelectionSort";}
@Override public void sort(int[] arr){
 int n=arr.length;
 for(int i=0;i<n-1;i++){
  int minIdx=i;
   for(int j=i+1;j<n;j++)if(arr[j]<arr[minIdx])minIdx=j;
  int t=arr[i];arr[i]=arr[minIdx];arr[minIdx]=t;
 }
}
}

class MergeSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "MergeSort";}
@Override public void sort(int[] arr){mergeSort(arr,0,arr.length-1);}

private void mergeSort(int[] arr,int lo,int hi){
 if(lo>=hi)return;
 int mid=(lo+hi)/2;
  mergeSort(arr,lo,mid);
  mergeSort(arr,mid+1,hi);
 merge(arr,lo,mid,hi);
}

private void merge(int[] arr,int lo,int mid,int hi){
 int n1=mid-lo+1,n2=hi-mid;
 int[] L=new int[n1],R=new int[n2];
  
  System.arraycopy(arr,lo,L,0,n1);
 System.arraycopy(arr,mid+1,R,0,n2);
 
 int i=0,j=0,k=lo;
  while(i<n1&&j<n2)arr[k++]=(L[i]<=R[j])?L[i++]:R[j++];
 
 while(i<n1)arr[k++]=L[i++];
  while(j<n2)arr[k++]=R[j++];
}
}

class QuickSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "QuickSort";}
@Override public void sort(int[] arr){quickSort(arr,0,arr.length-1);}

private void quickSort(int[] arr,int lo,int hi){
 if(lo>=hi)return;
 int p=partition(arr,lo,hi);
  quickSort(arr,lo,p-1);
 quickSort(arr,p+1,hi);
}

private int partition(int[] arr,int lo,int hi){
 int mid=(lo+hi)/2;
  
 if(arr[lo]>arr[mid]){int t=arr[lo];arr[lo]=arr[mid];arr[mid]=t;}
  if(arr[lo]>arr[hi]){int t=arr[lo];arr[lo]=arr[hi];arr[hi]=t;}
 if(arr[mid]>arr[hi]){int t=arr[mid];arr[mid]=arr[hi];arr[hi]=t;}
 
 int pivot=arr[hi];
 int i=lo-1;
  
 for(int j=lo;j<hi;j++){
  if(arr[j]<=pivot){i++;int tmp=arr[i];arr[i]=arr[j];arr[j]=tmp;}
 }
 
 int tmp=arr[i+1];arr[i+1]=arr[hi];arr[hi]=tmp;
 return i+1;
}
}

class HeapSortAlgorithm implements SortingAlgorithm{
@Override public String name(){return "HeapSort";}
@Override public void sort(int[] arr){
 int n=arr.length;
  
 for(int i=n/2-1;i>=0;i--)heapify(arr,n,i);
  
 for(int i=n-1;i>0;i--){
  int t=arr[0];arr[0]=arr[i];arr[i]=t;
   heapify(arr,i,0);
 }
}

private void heapify(int[] arr,int n,int i){
 int largest=i,l=2*i+1,r=2*i+2;
  
 if(l<n&&arr[l]>arr[largest])largest=l;
  if(r<n&&arr[r]>arr[largest])largest=r;
 
 if(largest!=i){
  int t=arr[i];arr[i]=arr[largest];arr[largest]=t;
   heapify(arr,n,largest);
 }
}
}