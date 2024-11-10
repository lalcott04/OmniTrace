import pandas as pd
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler

# Function to preprocess the data (remove 'PackageName' and scale)
def preprocess_data(data):
    X = data.drop('PackageName', axis=1)  # Drop 'PackageName' for clustering
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)  # Standardizing the data
    return X_scaled

# Function to apply K-Means clustering
def apply_kmeans(X, num_clusters):
    kmeans = KMeans(n_clusters=num_clusters, random_state=42)
    kmeans.fit(X)
    return kmeans

# Function to load data and apply K-Means clustering, then return labels
def get_cluster_labels(file_path, num_clusters=2):
    data = pd.read_csv(file_path)  # Read CSV file
    X_scaled = preprocess_data(data)  # Preprocess data
    kmeans = apply_kmeans(X_scaled, num_clusters)  # Apply K-Means clustering
    return kmeans.labels_.tolist()  # Return cluster labels as a list
