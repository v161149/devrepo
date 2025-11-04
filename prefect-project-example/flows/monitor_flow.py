"""
System Monitoring Flow
This flow monitors system health during business hours.
Schedule: 9 AM, 12 PM, 3 PM, 6 PM on weekdays
"""

from prefect import flow, task
from datetime import datetime
import random


@task
def check_cpu_usage():
    """
    Check CPU usage.
    
    Returns:
        dict: CPU metrics
    """
    print(f"[{datetime.now()}] Checking CPU usage...")
    
    # Simulated CPU check - replace with actual system monitoring
    # import psutil
    # cpu_percent = psutil.cpu_percent(interval=1)
    
    cpu_percent = random.uniform(20, 85)  # Simulated value
    
    status = {
        'metric': 'CPU',
        'value': round(cpu_percent, 2),
        'unit': '%',
        'status': 'healthy' if cpu_percent < 80 else 'warning' if cpu_percent < 90 else 'critical',
        'threshold': 80
    }
    
    print(f"  CPU Usage: {status['value']}% - {status['status'].upper()}")
    return status


@task
def check_memory_usage():
    """
    Check memory usage.
    
    Returns:
        dict: Memory metrics
    """
    print(f"[{datetime.now()}] Checking memory usage...")
    
    # Simulated memory check
    # import psutil
    # memory = psutil.virtual_memory()
    # memory_percent = memory.percent
    
    memory_percent = random.uniform(30, 75)  # Simulated value
    
    status = {
        'metric': 'Memory',
        'value': round(memory_percent, 2),
        'unit': '%',
        'status': 'healthy' if memory_percent < 75 else 'warning' if memory_percent < 85 else 'critical',
        'threshold': 75
    }
    
    print(f"  Memory Usage: {status['value']}% - {status['status'].upper()}")
    return status


@task
def check_disk_usage():
    """
    Check disk space usage.
    
    Returns:
        dict: Disk metrics
    """
    print(f"[{datetime.now()}] Checking disk usage...")
    
    # Simulated disk check
    # import psutil
    # disk = psutil.disk_usage('/')
    # disk_percent = disk.percent
    
    disk_percent = random.uniform(40, 70)  # Simulated value
    
    status = {
        'metric': 'Disk',
        'value': round(disk_percent, 2),
        'unit': '%',
        'status': 'healthy' if disk_percent < 80 else 'warning' if disk_percent < 90 else 'critical',
        'threshold': 80
    }
    
    print(f"  Disk Usage: {status['value']}% - {status['status'].upper()}")
    return status


@task
def check_api_endpoints():
    """
    Check if critical API endpoints are responding.
    
    Returns:
        dict: API health metrics
    """
    print(f"[{datetime.now()}] Checking API endpoints...")
    
    # Simulated API checks - replace with actual health checks
    # import requests
    # response = requests.get('https://api.example.com/health')
    
    endpoints = [
        {'name': 'Main API', 'url': 'https://api.example.com', 'status': 'up', 'response_time': 120},
        {'name': 'Auth API', 'url': 'https://auth.example.com', 'status': 'up', 'response_time': 95},
        {'name': 'Data API', 'url': 'https://data.example.com', 'status': 'up', 'response_time': 150},
    ]
    
    all_up = all(ep['status'] == 'up' for ep in endpoints)
    
    status = {
        'metric': 'API Health',
        'endpoints_checked': len(endpoints),
        'endpoints_up': sum(1 for ep in endpoints if ep['status'] == 'up'),
        'status': 'healthy' if all_up else 'critical',
        'details': endpoints
    }
    
    print(f"  API Health: {status['endpoints_up']}/{status['endpoints_checked']} endpoints up")
    return status


@task
def check_database_connections():
    """
    Check database connection health.
    
    Returns:
        dict: Database health metrics
    """
    print(f"[{datetime.now()}] Checking database connections...")
    
    # Simulated database check
    # import psycopg2
    # conn = psycopg2.connect(...)
    
    databases = [
        {'name': 'Primary DB', 'status': 'connected', 'latency': 25},
        {'name': 'Replica DB', 'status': 'connected', 'latency': 30},
        {'name': 'Analytics DB', 'status': 'connected', 'latency': 45}
    ]
    
    all_connected = all(db['status'] == 'connected' for db in databases)
    
    status = {
        'metric': 'Database Health',
        'databases_checked': len(databases),
        'databases_connected': sum(1 for db in databases if db['status'] == 'connected'),
        'status': 'healthy' if all_connected else 'critical',
        'details': databases
    }
    
    print(f"  Database Health: {status['databases_connected']}/{status['databases_checked']} connected")
    return status


@task
def analyze_health_status(checks: list, alert_threshold: int):
    """
    Analyze all health checks and determine if alerts are needed.
    
    Args:
        checks: List of all health check results
        alert_threshold: Threshold for alerting
        
    Returns:
        dict: Analysis summary
    """
    print(f"[{datetime.now()}] Analyzing health status...")
    
    critical_issues = [c for c in checks if c.get('status') == 'critical']
    warning_issues = [c for c in checks if c.get('status') == 'warning']
    
    analysis = {
        'overall_status': 'critical' if critical_issues else 'warning' if warning_issues else 'healthy',
        'total_checks': len(checks),
        'critical_count': len(critical_issues),
        'warning_count': len(warning_issues),
        'healthy_count': len(checks) - len(critical_issues) - len(warning_issues),
        'critical_issues': [c.get('metric', 'Unknown') for c in critical_issues],
        'warning_issues': [c.get('metric', 'Unknown') for c in warning_issues],
        'requires_alert': len(critical_issues) > 0 or len(warning_issues) >= alert_threshold
    }
    
    print(f"  Overall Status: {analysis['overall_status'].upper()}")
    print(f"  Critical: {analysis['critical_count']}, Warning: {analysis['warning_count']}, Healthy: {analysis['healthy_count']}")
    
    return analysis


@task
def send_alert(analysis: dict):
    """
    Send alert if issues are detected.
    
    Args:
        analysis: Health analysis results
        
    Returns:
        str: Alert status
    """
    if not analysis['requires_alert']:
        print(f"[{datetime.now()}] No alerts needed - system is healthy")
        return "No alerts sent"
    
    print(f"[{datetime.now()}] Sending alerts for {analysis['overall_status'].upper()} status...")
    
    # Simulated alert sending - replace with actual alerting (email, Slack, PagerDuty, etc.)
    alert_message = f"""
    SYSTEM HEALTH ALERT
    Status: {analysis['overall_status'].upper()}
    Critical Issues: {len(analysis['critical_issues'])}
    Warning Issues: {len(analysis['warning_issues'])}
    
    Critical: {', '.join(analysis['critical_issues']) if analysis['critical_issues'] else 'None'}
    Warnings: {', '.join(analysis['warning_issues']) if analysis['warning_issues'] else 'None'}
    """
    
    print(f"  Alert sent: {analysis['overall_status'].upper()}")
    return f"Alert sent for {analysis['overall_status']} status"


@flow(name="system-monitoring", log_prints=True)
def monitoring_flow(check_type: str, alert_threshold: int = 2):
    """
    Monitor system health and send alerts if needed.
    
    This flow checks various system metrics including CPU, memory, disk,
    API endpoints, and database connections.
    
    Args:
        check_type: Type of health check to perform
        alert_threshold: Number of warnings before alerting
        
    Returns:
        dict: Monitoring summary
    """
    print("=" * 50)
    print(f"Starting System Health Check: {check_type}")
    print("=" * 50)
    
    # Perform all health checks
    checks = []
    
    cpu_status = check_cpu_usage()
    checks.append(cpu_status)
    
    memory_status = check_memory_usage()
    checks.append(memory_status)
    
    disk_status = check_disk_usage()
    checks.append(disk_status)
    
    api_status = check_api_endpoints()
    checks.append(api_status)
    
    db_status = check_database_connections()
    checks.append(db_status)
    
    # Analyze results
    analysis = analyze_health_status(checks, alert_threshold)
    
    # Send alerts if needed
    alert_status = send_alert(analysis)
    
    print("=" * 50)
    print(f"Health Check Complete: {analysis['overall_status'].upper()}")
    print(f"Alert Status: {alert_status}")
    print("=" * 50)
    
    return {
        'checks': checks,
        'analysis': analysis,
        'alert_status': alert_status
    }


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    monitoring_flow(
        check_type="system_health",
        alert_threshold=2
    )
