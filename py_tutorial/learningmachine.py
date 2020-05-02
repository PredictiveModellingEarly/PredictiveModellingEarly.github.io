import pandas as pd
from sklearn import preprocessing
from sklearn.linear_model import LogisticRegression

def label_encode(df, column_name):
    """
    Transform non-numerical values to numerical labels.
    
    Keyword arguments:
    df -- Pandas dataframe
    column_name -- name of column (as string) in dataframe whose values to label-encode
    """    
    le = preprocessing.LabelEncoder()
    df[column_name] = le.fit_transform(df[column_name])


def df_to_list(df):
    """
    Convert Pandas dataframe into a list of lists.
    """
    return df.values.tolist()


def columnname_to_index(df):
    """
    Return a list of tuples in the format
    [(0, feat_name_0),
     (1, feat_name_0),
     ...
     (n-1, feat_name_n_minus_1)]
    
    The features are the column names of the Pandas dataframe df.
    """
    feature_indices = []
    for i, column_name in enumerate(df.columns):
        feature_indices.append((i, column_name))
    return feature_indices


def learning_machine(x_data, y_data, predictors):
    """
    Fit the Logistic Regression Classifier using training data and output the fitted model and a list of the model's coefficients.
    
    Keyword arguments:
    x_data -- list of lists containing predictors from training data
    y_data -- list of lists containing outcome from training data
    predictors -- list containing predictors of interest
    """    
    fit = LogisticRegression(random_state=0)
    model = fit.fit(x_data, y_data)
    coefs = [coef for sublist in model.coef_.tolist() for coef in sublist]
    labeled_coefs = list(zip(predictors, coefs))
    model.coefs = labeled_coefs

    return model

def predict(model, x_data):
    """
    Predict the outcome (either 0 or 1).
    
    Keyword arguments:
    x_data -- list of lists containing features
    model -- model generated by learning machine
    """
    return model.predict_proba(x_data)[:,1].tolist()
