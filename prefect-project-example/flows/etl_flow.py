"""
ETL Flow Example
This flow demonstrates a simple Extract, Transform, Load pipeline.
Schedule: Daily at 2 AM
"""

from prefect import flow, task
import pandas as pd
import requests
from datetime import datetime


@task(retries=3, retry_delay_seconds=30)
def extract_data(api_url: str):
    """
    Extract data from an API endpoint.
    
    Args:
        api_url: The API endpoint to fetch data from
        
    Returns:
        dict: The extracted data
    """
    print(f"[{datetime.now()}] Extracting data from {api_url}")
    
    # Simulated API call - replace with your actual API logic
    # response = requests.get(api_url)
    # response.raise_for_status()
    # data = response.json()
    
    # Simulated data for demonstration
    data = {
        'records': [
            {'id': 1, 'name': 'Product A', 'value': 100, 'status': 'active'},
            {'id': 2, 'name': 'Product B', 'value': 200, 'status': 'active'},
            {'id': 3, 'name': 'Product C', 'value': 150, 'status': 'inactive'},
            {'id': 4, 'name': 'Product D', 'value': 300, 'status': 'active'},
        ]
    }
    
    print(f"✓ Extracted {len(data['records'])} records")
    return data


@task
def transform_data(raw_data: dict):
    """
    Transform the raw data into a clean format.
    
    Args:
        raw_data: The raw data from extraction
        
    Returns:
        pd.DataFrame: Transformed data
    """
    print(f"[{datetime.now()}] Transforming data...")
    
    # Convert to DataFrame
    df = pd.DataFrame(raw_data['records'])
    
    # Data transformations
    df['processed_date'] = datetime.now()
    df['value_category'] = df['value'].apply(
        lambda x: 'high' if x >= 200 else 'medium' if x >= 150 else 'low'
    )
    
    # Filter only active records
    df = df[df['status'] == 'active']
    
    # Add calculated fields
    df['value_squared'] = df['value'] ** 2
    
    print(f"✓ Transformed data: {len(df)} rows, {len(df.columns)} columns")
    return df


@task(retries=2)
def load_data(df: pd.DataFrame, output_path: str):
    """
    Load the transformed data to a destination.
    
    Args:
        df: The transformed DataFrame
        output_path: Path where to save the data
        
    Returns:
        str: Confirmation message
    """
    print(f"[{datetime.now()}] Loading data to {output_path}")
    
    # Save to CSV - replace with your actual database/storage logic
    try:
        df.to_csv(output_path, index=False)
        print(f"✓ Successfully saved {len(df)} rows to {output_path}")
    except Exception as e:
        print(f"Note: Could not save to {output_path}: {e}")
        print("In production, this would write to your database or data warehouse")
    
    return f"Loaded {len(df)} rows successfully"


@flow(name="etl-pipeline", log_prints=True)
def etl_flow(api_url: str, output_path: str):
    """
    Complete ETL pipeline flow.
    
    This flow extracts data from an API, transforms it,
    and loads it to a destination.
    
    Args:
        api_url: The API endpoint to extract data from
        output_path: The destination path for the processed data
        
    Returns:
        str: Summary of the ETL process
    """
    print("=" * 50)
    print("Starting ETL Pipeline")
    print("=" * 50)
    
    # Extract
    raw_data = extract_data(api_url)
    
    # Transform
    transformed_data = transform_data(raw_data)
    
    # Load
    result = load_data(transformed_data, output_path)
    
    print("=" * 50)
    print(f"ETL Pipeline Complete: {result}")
    print("=" * 50)
    
    return result


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    etl_flow(
        api_url="https://api.example.com/data",
        output_path="C:/temp/etl_output.csv"
    )
