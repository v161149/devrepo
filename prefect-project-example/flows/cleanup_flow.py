"""
Cleanup Flow
This flow performs monthly cleanup of old files and data.
Schedule: First day of every month at 3 AM
"""

from prefect import flow, task
from datetime import datetime, timedelta
import os
from pathlib import Path


@task
def scan_directory(target_directory: str, retention_days: int):
    """
    Scan directory for files older than retention period.
    
    Args:
        target_directory: Directory to scan
        retention_days: Number of days to retain files
        
    Returns:
        dict: Scan results
    """
    print(f"[{datetime.now()}] Scanning directory: {target_directory}")
    print(f"  Retention policy: {retention_days} days")
    
    # Simulated directory scan - replace with actual file system operations
    # In production, this would actually scan the directory
    # cutoff_date = datetime.now() - timedelta(days=retention_days)
    
    # Simulated results
    files_found = {
        'total_files': 450,
        'old_files': 125,
        'total_size_mb': 2500,
        'old_files_size_mb': 850,
        'file_types': {
            'logs': 80,
            'temp': 30,
            'backups': 15
        }
    }
    
    print(f"✓ Scan complete: {files_found['old_files']} files eligible for cleanup")
    print(f"  Total size to be freed: {files_found['old_files_size_mb']} MB")
    
    return files_found


@task
def create_backup_list(files_found: dict, dry_run: bool):
    """
    Create a list of files to back up before deletion.
    
    Args:
        files_found: Results from directory scan
        dry_run: If True, only simulate the backup
        
    Returns:
        list: List of files to backup
    """
    print(f"[{datetime.now()}] Creating backup list...")
    
    # In production, this would create actual backup list
    # prioritizing important files
    
    backup_list = [
        {'file': 'important_log_2024-10-01.txt', 'size_kb': 2048},
        {'file': 'config_backup_2024-10-05.json', 'size_kb': 512},
        {'file': 'data_export_2024-10-10.csv', 'size_kb': 15360}
    ]
    
    if dry_run:
        print("  DRY RUN: No actual backup will be created")
    
    print(f"✓ {len(backup_list)} files marked for backup before deletion")
    return backup_list


@task(retries=2, retry_delay_seconds=60)
def backup_files(backup_list: list, dry_run: bool):
    """
    Backup files before deletion.
    
    Args:
        backup_list: List of files to backup
        dry_run: If True, only simulate the backup
        
    Returns:
        str: Backup status
    """
    print(f"[{datetime.now()}] Backing up {len(backup_list)} files...")
    
    if dry_run:
        print("  DRY RUN: Skipping actual backup")
        return "Backup skipped (dry run)"
    
    # Simulated backup - replace with actual backup logic
    # import shutil
    # for file in backup_list:
    #     shutil.copy2(file['file'], backup_location)
    
    total_size = sum(f['size_kb'] for f in backup_list)
    print(f"✓ Backup complete: {len(backup_list)} files, {total_size/1024:.2f} MB")
    
    return f"Backed up {len(backup_list)} files"


@task
def delete_old_files(files_found: dict, dry_run: bool):
    """
    Delete old files.
    
    Args:
        files_found: Information about files to delete
        dry_run: If True, only simulate deletion
        
    Returns:
        dict: Deletion results
    """
    print(f"[{datetime.now()}] Deleting old files...")
    
    if dry_run:
        print("  DRY RUN: No files will be actually deleted")
        results = {
            'files_deleted': 0,
            'would_delete': files_found['old_files'],
            'space_freed_mb': 0,
            'would_free_mb': files_found['old_files_size_mb'],
            'dry_run': True
        }
        print(f"  Would delete: {results['would_delete']} files ({results['would_free_mb']} MB)")
        return results
    
    # Simulated deletion - replace with actual file deletion
    # import os
    # for file in old_files:
    #     os.remove(file)
    
    results = {
        'files_deleted': files_found['old_files'],
        'space_freed_mb': files_found['old_files_size_mb'],
        'dry_run': False
    }
    
    print(f"✓ Deleted {results['files_deleted']} files")
    print(f"✓ Freed {results['space_freed_mb']} MB of disk space")
    
    return results


@task
def optimize_remaining_files(target_directory: str, dry_run: bool):
    """
    Optimize and compress remaining files.
    
    Args:
        target_directory: Directory to optimize
        dry_run: If True, only simulate optimization
        
    Returns:
        dict: Optimization results
    """
    print(f"[{datetime.now()}] Optimizing remaining files...")
    
    if dry_run:
        print("  DRY RUN: No optimization will be performed")
        return {'optimized': 0, 'space_saved_mb': 0, 'dry_run': True}
    
    # Simulated optimization - replace with actual optimization
    # This could include compressing logs, archiving old data, etc.
    
    results = {
        'files_optimized': 75,
        'space_saved_mb': 150,
        'dry_run': False
    }
    
    print(f"✓ Optimized {results['files_optimized']} files")
    print(f"✓ Additional space saved: {results['space_saved_mb']} MB")
    
    return results


@task
def generate_cleanup_report(scan_results: dict, delete_results: dict, optimize_results: dict):
    """
    Generate a summary report of the cleanup operation.
    
    Args:
        scan_results: Results from directory scan
        delete_results: Results from file deletion
        optimize_results: Results from optimization
        
    Returns:
        str: Formatted report
    """
    print(f"[{datetime.now()}] Generating cleanup report...")
    
    total_space_freed = delete_results.get('space_freed_mb', 0) + optimize_results.get('space_saved_mb', 0)
    
    report = f"""
    ==========================================
    MONTHLY CLEANUP REPORT
    ==========================================
    
    Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
    
    SCAN RESULTS:
    - Total files scanned: {scan_results['total_files']}
    - Files eligible for cleanup: {scan_results['old_files']}
    - Total directory size: {scan_results['total_size_mb']} MB
    
    DELETION RESULTS:
    - Files deleted: {delete_results.get('files_deleted', 0)}
    - Space freed: {delete_results.get('space_freed_mb', 0)} MB
    - Dry run: {delete_results.get('dry_run', False)}
    
    OPTIMIZATION RESULTS:
    - Files optimized: {optimize_results.get('files_optimized', 0)}
    - Additional space saved: {optimize_results.get('space_saved_mb', 0)} MB
    
    TOTAL SPACE RECOVERED: {total_space_freed} MB
    
    FILE TYPE BREAKDOWN:
    """
    
    for file_type, count in scan_results['file_types'].items():
        report += f"    - {file_type}: {count} files\n"
    
    report += "\n    =========================================="
    
    print("✓ Report generated")
    return report


@flow(name="cleanup-flow", log_prints=True)
def cleanup_flow(retention_days: int, target_directory: str, dry_run: bool = False):
    """
    Perform monthly cleanup of old files.
    
    This flow scans for old files, backs them up, deletes them,
    and optimizes remaining files to free up disk space.
    
    Args:
        retention_days: Number of days to retain files
        target_directory: Directory to clean up
        dry_run: If True, simulate cleanup without actually deleting files
        
    Returns:
        str: Cleanup summary report
    """
    print("=" * 50)
    print(f"Starting Monthly Cleanup")
    if dry_run:
        print("** DRY RUN MODE - No files will be deleted **")
    print("=" * 50)
    
    # Scan directory
    scan_results = scan_directory(target_directory, retention_days)
    
    # Create backup list
    backup_list = create_backup_list(scan_results, dry_run)
    
    # Backup important files
    backup_status = backup_files(backup_list, dry_run)
    
    # Delete old files
    delete_results = delete_old_files(scan_results, dry_run)
    
    # Optimize remaining files
    optimize_results = optimize_remaining_files(target_directory, dry_run)
    
    # Generate report
    report = generate_cleanup_report(scan_results, delete_results, optimize_results)
    
    print("\n" + report + "\n")
    
    print("=" * 50)
    if dry_run:
        print("Cleanup Complete (DRY RUN)")
    else:
        total_freed = delete_results.get('space_freed_mb', 0) + optimize_results.get('space_saved_mb', 0)
        print(f"Cleanup Complete: {total_freed} MB freed")
    print("=" * 50)
    
    return report


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally (with dry_run=True for safety)
    cleanup_flow(
        retention_days=90,
        target_directory="C:/temp/old_files",
        dry_run=True  # Set to False to actually delete files
    )
