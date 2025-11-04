"""
Database Sync Flow Example
This flow synchronizes data between two databases.
Schedule: Every hour
"""

from prefect import flow, task
from datetime import datetime
import time


@task(retries=3, retry_delay_seconds=60)
def check_source_connection(source: str):
    """
    Check if source database is accessible.
    
    Args:
        source: Source database identifier
        
    Returns:
        bool: True if connection successful
    """
    print(f"[{datetime.now()}] Checking connection to source: {source}")
    
    # Simulated connection check - replace with actual database connection
    # import psycopg2
    # conn = psycopg2.connect(...)
    # conn.close()
    
    time.sleep(1)  # Simulate connection time
    print(f"✓ Source {source} is accessible")
    return True


@task(retries=3, retry_delay_seconds=60)
def check_destination_connection(destination: str):
    """
    Check if destination database is accessible.
    
    Args:
        destination: Destination database identifier
        
    Returns:
        bool: True if connection successful
    """
    print(f"[{datetime.now()}] Checking connection to destination: {destination}")
    
    # Simulated connection check
    time.sleep(1)
    print(f"✓ Destination {destination} is accessible")
    return True


@task
def get_sync_status(source: str, destination: str):
    """
    Check the last sync time and determine what needs to be synced.
    
    Args:
        source: Source database
        destination: Destination database
        
    Returns:
        dict: Sync status information
    """
    print(f"[{datetime.now()}] Checking sync status...")
    
    # Simulated sync status check
    # In production, this would query metadata tables
    status = {
        'last_sync': '2024-01-01 01:00:00',
        'records_to_sync': 150,
        'tables_to_sync': ['users', 'orders', 'products']
    }
    
    print(f"✓ Found {status['records_to_sync']} records to sync")
    return status


@task(retries=2)
def sync_data(source: str, destination: str, sync_status: dict):
    """
    Perform the actual data synchronization.
    
    Args:
        source: Source database
        destination: Destination database
        sync_status: Information about what to sync
        
    Returns:
        dict: Sync results
    """
    print(f"[{datetime.now()}] Starting data sync...")
    
    records_synced = 0
    
    # Simulated sync process
    for table in sync_status['tables_to_sync']:
        print(f"  Syncing table: {table}")
        time.sleep(0.5)  # Simulate sync time
        records_synced += 50
        print(f"  ✓ Synced {table}")
    
    results = {
        'records_synced': records_synced,
        'tables_synced': len(sync_status['tables_to_sync']),
        'sync_time': datetime.now().isoformat(),
        'status': 'success'
    }
    
    print(f"✓ Sync complete: {records_synced} records synced")
    return results


@task
def update_sync_metadata(results: dict):
    """
    Update metadata about the sync operation.
    
    Args:
        results: Results from the sync operation
        
    Returns:
        str: Confirmation message
    """
    print(f"[{datetime.now()}] Updating sync metadata...")
    
    # In production, this would update a metadata table
    print(f"✓ Metadata updated: {results['status']}")
    return "Metadata updated successfully"


@flow(name="database-sync", log_prints=True)
def sync_flow(source: str, destination: str):
    """
    Synchronize data between two databases.
    
    This flow checks connections, determines what needs to be synced,
    performs the sync, and updates metadata.
    
    Args:
        source: Source database identifier
        destination: Destination database identifier
        
    Returns:
        dict: Summary of sync operation
    """
    print("=" * 50)
    print(f"Starting Database Sync: {source} → {destination}")
    print("=" * 50)
    
    # Check connections
    source_ok = check_source_connection(source)
    dest_ok = check_destination_connection(destination)
    
    if not (source_ok and dest_ok):
        raise Exception("Connection check failed")
    
    # Get sync status
    sync_status = get_sync_status(source, destination)
    
    # Perform sync
    results = sync_data(source, destination, sync_status)
    
    # Update metadata
    update_sync_metadata(results)
    
    print("=" * 50)
    print(f"Sync Complete: {results['records_synced']} records")
    print("=" * 50)
    
    return results


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    sync_flow(
        source="database_a",
        destination="database_b"
    )
