"""
Test All Flows Script
This script allows you to test all flows locally before deploying.
"""

import sys
from pathlib import Path

# Add the flows directory to the path
flows_dir = Path(__file__).parent.parent / "flows"
sys.path.insert(0, str(flows_dir))

from etl_flow import etl_flow
from sync_flow import sync_flow
from report_flow import generate_report
from monitor_flow import monitoring_flow
from cleanup_flow import cleanup_flow
from import_flow import import_data
from quick_check import quick_check_flow
from complex_flow import complex_flow


def test_etl_flow():
    """Test the ETL flow"""
    print("\n" + "="*60)
    print("TESTING: ETL Flow")
    print("="*60)
    try:
        result = etl_flow(
            api_url="https://api.example.com/data",
            output_path="C:/temp/test_etl_output.csv"
        )
        print(f"✓ ETL Flow completed: {result}")
        return True
    except Exception as e:
        print(f"✗ ETL Flow failed: {e}")
        return False


def test_sync_flow():
    """Test the sync flow"""
    print("\n" + "="*60)
    print("TESTING: Sync Flow")
    print("="*60)
    try:
        result = sync_flow(
            source="database_a",
            destination="database_b"
        )
        print(f"✓ Sync Flow completed: {result}")
        return True
    except Exception as e:
        print(f"✗ Sync Flow failed: {e}")
        return False


def test_report_flow():
    """Test the report flow"""
    print("\n" + "="*60)
    print("TESTING: Report Flow")
    print("="*60)
    try:
        result = generate_report(
            report_type="weekly_summary",
            recipients=["test@example.com"]
        )
        print(f"✓ Report Flow completed: {result}")
        return True
    except Exception as e:
        print(f"✗ Report Flow failed: {e}")
        return False


def test_monitor_flow():
    """Test the monitoring flow"""
    print("\n" + "="*60)
    print("TESTING: Monitor Flow")
    print("="*60)
    try:
        result = monitoring_flow(
            check_type="system_health",
            alert_threshold=2
        )
        print(f"✓ Monitor Flow completed")
        return True
    except Exception as e:
        print(f"✗ Monitor Flow failed: {e}")
        return False


def test_cleanup_flow():
    """Test the cleanup flow"""
    print("\n" + "="*60)
    print("TESTING: Cleanup Flow (DRY RUN)")
    print("="*60)
    try:
        result = cleanup_flow(
            retention_days=90,
            target_directory="C:/temp/old_files",
            dry_run=True  # Always use dry_run=True for testing!
        )
        print(f"✓ Cleanup Flow completed (dry run)")
        return True
    except Exception as e:
        print(f"✗ Cleanup Flow failed: {e}")
        return False


def test_import_flow():
    """Test the import flow"""
    print("\n" + "="*60)
    print("TESTING: Import Flow")
    print("="*60)
    try:
        result = import_data(
            import_source="C:/temp/test_import.csv",
            validate=True
        )
        print(f"✓ Import Flow completed")
        return True
    except Exception as e:
        print(f"✗ Import Flow failed: {e}")
        return False


def test_quick_check_flow():
    """Test the quick check flow"""
    print("\n" + "="*60)
    print("TESTING: Quick Check Flow")
    print("="*60)
    try:
        result = quick_check_flow(
            check_interval="30min",
            timeout=60
        )
        print(f"✓ Quick Check Flow completed")
        return True
    except Exception as e:
        print(f"✗ Quick Check Flow failed: {e}")
        return False


def test_complex_flow():
    """Test the complex schedule flow"""
    print("\n" + "="*60)
    print("TESTING: Complex Schedule Flow")
    print("="*60)
    try:
        result = complex_flow(
            mode="production",
            max_retries=3
        )
        print(f"✓ Complex Flow completed")
        return True
    except Exception as e:
        print(f"✗ Complex Flow failed: {e}")
        return False


def main():
    """Run all flow tests"""
    print("\n" + "="*60)
    print("PREFECT FLOW TEST SUITE")
    print("="*60)
    print("\nThis script will test all flows locally before deployment.")
    print("Make sure you have installed all dependencies:")
    print("  pip install -r requirements.txt\n")
    
    input("Press Enter to start testing...")
    
    tests = [
        ("ETL Flow", test_etl_flow),
        ("Sync Flow", test_sync_flow),
        ("Report Flow", test_report_flow),
        ("Monitor Flow", test_monitor_flow),
        ("Cleanup Flow", test_cleanup_flow),
        ("Import Flow", test_import_flow),
        ("Quick Check Flow", test_quick_check_flow),
        ("Complex Flow", test_complex_flow)
    ]
    
    results = {}
    for test_name, test_func in tests:
        results[test_name] = test_func()
    
    # Print summary
    print("\n" + "="*60)
    print("TEST SUMMARY")
    print("="*60)
    
    passed = sum(1 for r in results.values() if r)
    failed = len(results) - passed
    
    for test_name, result in results.items():
        status = "✓ PASSED" if result else "✗ FAILED"
        print(f"{test_name:.<40} {status}")
    
    print("="*60)
    print(f"Total: {passed} passed, {failed} failed out of {len(results)} tests")
    print("="*60)
    
    if failed == 0:
        print("\n✓ All flows are working correctly!")
        print("You can now deploy them using: prefect deploy --all")
    else:
        print(f"\n⚠ {failed} flow(s) failed. Please fix the issues before deploying.")
    
    return failed == 0


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
