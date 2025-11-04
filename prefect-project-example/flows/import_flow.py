"""
Manual Data Import Flow
This flow handles manual data imports triggered on-demand.
Schedule: None (manual trigger only)
"""

from prefect import flow, task
from datetime import datetime
import pandas as pd


@task
def validate_import_file(import_source: str):
    """
    Validate the import file before processing.
    
    Args:
        import_source: Source of the import data
        
    Returns:
        dict: Validation results
    """
    print(f"[{datetime.now()}] Validating import file from {import_source}...")
    
    # Simulated validation - replace with actual file validation
    # This would check:
    # - File exists
    # - File format is correct
    # - Required columns are present
    # - Data types are correct
    
    validation_results = {
        'valid': True,
        'file_exists': True,
        'format_correct': True,
        'required_columns_present': True,
        'row_count': 1000,
        'column_count': 8,
        'errors': [],
        'warnings': ['Column "description" has 5 null values']
    }
    
    if not validation_results['valid']:
        print("✗ Validation failed!")
        for error in validation_results['errors']:
            print(f"  ERROR: {error}")
        raise ValueError("Import file validation failed")
    
    print("✓ Validation successful")
    if validation_results['warnings']:
        print("  Warnings:")
        for warning in validation_results['warnings']:
            print(f"    - {warning}")
    
    return validation_results


@task
def preview_data(import_source: str, validate: bool):
    """
    Preview the data to be imported.
    
    Args:
        import_source: Source of the import data
        validate: Whether to run additional validation
        
    Returns:
        dict: Data preview
    """
    print(f"[{datetime.now()}] Generating data preview...")
    
    # Simulated data preview - replace with actual data loading
    # df = pd.read_csv(import_source)
    # preview = df.head(10).to_dict('records')
    
    preview = {
        'sample_rows': [
            {'id': 1, 'name': 'Item A', 'value': 100, 'category': 'Electronics'},
            {'id': 2, 'name': 'Item B', 'value': 200, 'category': 'Books'},
            {'id': 3, 'name': 'Item C', 'value': 150, 'category': 'Clothing'},
            {'id': 4, 'name': 'Item D', 'value': 300, 'category': 'Electronics'},
            {'id': 5, 'name': 'Item E', 'value': 75, 'category': 'Books'}
        ],
        'total_rows': 1000,
        'data_types': {
            'id': 'integer',
            'name': 'string',
            'value': 'float',
            'category': 'string'
        },
        'unique_categories': ['Electronics', 'Books', 'Clothing', 'Home & Garden'],
        'value_range': {'min': 50, 'max': 500}
    }
    
    print(f"✓ Preview generated: {len(preview['sample_rows'])} sample rows")
    print(f"  Total rows to import: {preview['total_rows']}")
    
    return preview


@task
def check_for_duplicates(preview_data: dict):
    """
    Check if imported data contains duplicates with existing data.
    
    Args:
        preview_data: Preview of the import data
        
    Returns:
        dict: Duplicate check results
    """
    print(f"[{datetime.now()}] Checking for duplicate records...")
    
    # Simulated duplicate check - replace with actual database query
    # This would compare import data against existing database records
    
    duplicate_check = {
        'duplicates_found': 15,
        'duplicate_ids': [10, 25, 37, 42, 55, 68, 79, 88, 91, 105, 120, 135, 148, 156, 172],
        'action': 'skip',  # Options: skip, update, or error
        'unique_records': 985
    }
    
    if duplicate_check['duplicates_found'] > 0:
        print(f"⚠ Found {duplicate_check['duplicates_found']} duplicate records")
        print(f"  Action: {duplicate_check['action'].upper()}")
    else:
        print("✓ No duplicates found")
    
    return duplicate_check


@task(retries=2, retry_delay_seconds=30)
def import_data_to_database(preview_data: dict, duplicate_check: dict):
    """
    Import the validated data into the database.
    
    Args:
        preview_data: Preview of the import data
        duplicate_check: Results from duplicate checking
        
    Returns:
        dict: Import results
    """
    print(f"[{datetime.now()}] Importing data to database...")
    
    # Simulated import - replace with actual database import
    # import psycopg2
    # conn = psycopg2.connect(...)
    # cursor = conn.cursor()
    # for row in data:
    #     cursor.execute("INSERT INTO ...", row)
    # conn.commit()
    
    import_results = {
        'total_rows': preview_data['total_rows'],
        'rows_imported': duplicate_check['unique_records'],
        'rows_skipped': duplicate_check['duplicates_found'],
        'rows_failed': 0,
        'import_time_seconds': 45,
        'status': 'success'
    }
    
    print(f"✓ Import complete")
    print(f"  Rows imported: {import_results['rows_imported']}")
    print(f"  Rows skipped: {import_results['rows_skipped']}")
    print(f"  Import time: {import_results['import_time_seconds']} seconds")
    
    return import_results


@task
def update_metadata(import_results: dict, import_source: str):
    """
    Update metadata about the import operation.
    
    Args:
        import_results: Results from the import
        import_source: Source of the import
        
    Returns:
        str: Metadata update confirmation
    """
    print(f"[{datetime.now()}] Updating import metadata...")
    
    # Simulated metadata update - replace with actual metadata table update
    metadata = {
        'import_id': f"IMP-{datetime.now().strftime('%Y%m%d-%H%M%S')}",
        'import_date': datetime.now().isoformat(),
        'source': import_source,
        'rows_imported': import_results['rows_imported'],
        'status': import_results['status']
    }
    
    print(f"✓ Metadata updated: {metadata['import_id']}")
    return f"Metadata updated: {metadata['import_id']}"


@task
def send_import_notification(import_results: dict):
    """
    Send notification about the import completion.
    
    Args:
        import_results: Results from the import
        
    Returns:
        str: Notification status
    """
    print(f"[{datetime.now()}] Sending import completion notification...")
    
    # Simulated notification - replace with actual email/Slack notification
    notification_message = f"""
    Data Import Complete
    
    Status: {import_results['status'].upper()}
    Rows Imported: {import_results['rows_imported']}
    Rows Skipped: {import_results['rows_skipped']}
    Time: {import_results['import_time_seconds']} seconds
    """
    
    print("✓ Notification sent")
    return "Notification sent successfully"


@flow(name="manual-import", log_prints=True)
def import_data(import_source: str, validate: bool = True):
    """
    Manually import data from an external source.
    
    This flow validates, previews, checks for duplicates,
    imports the data, and sends notifications.
    
    Args:
        import_source: Source of the import data (file path, URL, etc.)
        validate: Whether to perform validation checks
        
    Returns:
        dict: Import summary
    """
    print("=" * 50)
    print(f"Starting Manual Data Import")
    print(f"Source: {import_source}")
    print("=" * 50)
    
    # Validate import file
    validation_results = validate_import_file(import_source)
    
    # Preview data
    preview_data_result = preview_data(import_source, validate)
    
    # Check for duplicates
    duplicate_check = check_for_duplicates(preview_data_result)
    
    # Import data
    import_results = import_data_to_database(preview_data_result, duplicate_check)
    
    # Update metadata
    metadata_status = update_metadata(import_results, import_source)
    
    # Send notification
    notification_status = send_import_notification(import_results)
    
    print("=" * 50)
    print(f"Import Complete: {import_results['rows_imported']} rows imported")
    print(f"Status: {import_results['status'].upper()}")
    print("=" * 50)
    
    return {
        'validation': validation_results,
        'import_results': import_results,
        'metadata_status': metadata_status,
        'notification_status': notification_status
    }


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    import_data(
        import_source="C:/imports/manual_data.csv",
        validate=True
    )
