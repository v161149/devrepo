"""
Quick Check Flow
This flow performs frequent system checks every 30 minutes.
Schedule: Every 30 minutes
"""

from prefect import flow, task
from datetime import datetime
import random


@task
def ping_critical_services():
    """
    Quickly ping all critical services.
    
    Returns:
        dict: Service status
    """
    print(f"[{datetime.now()}] Pinging critical services...")
    
    # Simulated service ping - replace with actual health checks
    services = {
        'web_server': {'status': 'up', 'response_ms': 25},
        'api_gateway': {'status': 'up', 'response_ms': 18},
        'database': {'status': 'up', 'response_ms': 12},
        'cache': {'status': 'up', 'response_ms': 8},
        'queue': {'status': 'up', 'response_ms': 15}
    }
    
    all_up = all(s['status'] == 'up' for s in services.values())
    avg_response = sum(s['response_ms'] for s in services.values()) / len(services)
    
    result = {
        'services': services,
        'all_up': all_up,
        'services_up': sum(1 for s in services.values() if s['status'] == 'up'),
        'total_services': len(services),
        'avg_response_ms': round(avg_response, 2)
    }
    
    print(f"✓ {result['services_up']}/{result['total_services']} services up")
    print(f"  Avg response time: {result['avg_response_ms']}ms")
    
    return result


@task
def check_error_rates():
    """
    Check recent error rates from logs.
    
    Returns:
        dict: Error rate information
    """
    print(f"[{datetime.now()}] Checking error rates...")
    
    # Simulated error rate check - replace with actual log analysis
    # This would typically query your logging system (ELK, Splunk, etc.)
    
    error_rate = random.uniform(0.1, 2.5)  # Errors per minute
    
    result = {
        'errors_per_minute': round(error_rate, 2),
        'threshold': 5.0,
        'status': 'normal' if error_rate < 5.0 else 'elevated',
        'last_30min_errors': int(error_rate * 30),
        'error_types': {
            '500_errors': int(error_rate * 10),
            '404_errors': int(error_rate * 15),
            'timeout_errors': int(error_rate * 5)
        }
    }
    
    print(f"  Error rate: {result['errors_per_minute']}/min - {result['status'].upper()}")
    
    return result


@task
def check_response_times():
    """
    Check API response times.
    
    Returns:
        dict: Response time metrics
    """
    print(f"[{datetime.now()}] Checking response times...")
    
    # Simulated response time check
    p50 = random.uniform(50, 120)
    p95 = random.uniform(150, 300)
    p99 = random.uniform(300, 500)
    
    result = {
        'p50_ms': round(p50, 2),
        'p95_ms': round(p95, 2),
        'p99_ms': round(p99, 2),
        'threshold_p95': 500,
        'status': 'good' if p95 < 500 else 'degraded'
    }
    
    print(f"  P50: {result['p50_ms']}ms, P95: {result['p95_ms']}ms, P99: {result['p99_ms']}ms")
    print(f"  Status: {result['status'].upper()}")
    
    return result


@task
def check_queue_depth():
    """
    Check message queue depth.
    
    Returns:
        dict: Queue metrics
    """
    print(f"[{datetime.now()}] Checking queue depth...")
    
    # Simulated queue check - replace with actual queue monitoring
    # This would connect to RabbitMQ, Kafka, SQS, etc.
    
    queue_depth = random.randint(10, 500)
    
    result = {
        'queue_depth': queue_depth,
        'threshold': 1000,
        'status': 'normal' if queue_depth < 1000 else 'high',
        'processing_rate': random.randint(50, 150),
        'estimated_clear_time_minutes': queue_depth / random.randint(50, 150)
    }
    
    print(f"  Queue depth: {result['queue_depth']} - {result['status'].upper()}")
    
    return result


@task
def check_active_connections():
    """
    Check number of active connections.
    
    Returns:
        dict: Connection metrics
    """
    print(f"[{datetime.now()}] Checking active connections...")
    
    # Simulated connection check
    active_connections = random.randint(50, 300)
    
    result = {
        'active_connections': active_connections,
        'max_connections': 500,
        'utilization_percent': round((active_connections / 500) * 100, 2),
        'status': 'normal' if active_connections < 400 else 'high'
    }
    
    print(f"  Active connections: {result['active_connections']}/{result['max_connections']} ({result['utilization_percent']}%)")
    
    return result


@task
def evaluate_system_health(services: dict, errors: dict, response_times: dict, 
                          queue: dict, connections: dict, timeout: int):
    """
    Evaluate overall system health from all checks.
    
    Args:
        services: Service status results
        errors: Error rate results
        response_times: Response time results
        queue: Queue depth results
        connections: Connection results
        timeout: Maximum acceptable check duration
        
    Returns:
        dict: Overall health evaluation
    """
    print(f"[{datetime.now()}] Evaluating system health...")
    
    # Determine overall status
    issues = []
    warnings = []
    
    if not services['all_up']:
        issues.append("Some services are down")
    
    if errors['status'] == 'elevated':
        warnings.append(f"Elevated error rate: {errors['errors_per_minute']}/min")
    
    if response_times['status'] == 'degraded':
        warnings.append(f"Degraded response times: P95={response_times['p95_ms']}ms")
    
    if queue['status'] == 'high':
        warnings.append(f"High queue depth: {queue['queue_depth']} messages")
    
    if connections['status'] == 'high':
        warnings.append(f"High connection utilization: {connections['utilization_percent']}%")
    
    # Determine overall status
    if issues:
        overall_status = 'critical'
    elif warnings:
        overall_status = 'warning'
    else:
        overall_status = 'healthy'
    
    health = {
        'overall_status': overall_status,
        'issues': issues,
        'warnings': warnings,
        'checks_passed': sum([
            1 if services['all_up'] else 0,
            1 if errors['status'] == 'normal' else 0,
            1 if response_times['status'] == 'good' else 0,
            1 if queue['status'] == 'normal' else 0,
            1 if connections['status'] == 'normal' else 0
        ]),
        'total_checks': 5,
        'timestamp': datetime.now().isoformat()
    }
    
    print(f"  Overall Status: {health['overall_status'].upper()}")
    print(f"  Checks passed: {health['checks_passed']}/{health['total_checks']}")
    
    return health


@task
def log_check_results(health: dict):
    """
    Log the check results for historical tracking.
    
    Args:
        health: Overall health evaluation
        
    Returns:
        str: Logging confirmation
    """
    print(f"[{datetime.now()}] Logging check results...")
    
    # In production, this would write to a time-series database
    # or monitoring system for historical tracking and alerting
    
    print("✓ Results logged to monitoring system")
    return "Results logged successfully"


@flow(name="quick-check", log_prints=True)
def quick_check_flow(check_interval: str = "30min", timeout: int = 60):
    """
    Perform quick system health checks.
    
    This lightweight flow runs frequently to catch issues quickly.
    It checks services, error rates, response times, queues, and connections.
    
    Args:
        check_interval: How often this check runs
        timeout: Maximum time allowed for checks
        
    Returns:
        dict: Health check summary
    """
    print("=" * 50)
    print(f"Starting Quick Health Check (Interval: {check_interval})")
    print("=" * 50)
    
    # Perform all quick checks
    services = ping_critical_services()
    errors = check_error_rates()
    response_times = check_response_times()
    queue = check_queue_depth()
    connections = check_active_connections()
    
    # Evaluate overall health
    health = evaluate_system_health(
        services, errors, response_times, queue, connections, timeout
    )
    
    # Log results
    log_status = log_check_results(health)
    
    # Print summary
    print("\n" + "-" * 50)
    print("HEALTH CHECK SUMMARY")
    print("-" * 50)
    print(f"Status: {health['overall_status'].upper()}")
    print(f"Checks passed: {health['checks_passed']}/{health['total_checks']}")
    
    if health['issues']:
        print("\nIssues:")
        for issue in health['issues']:
            print(f"  ✗ {issue}")
    
    if health['warnings']:
        print("\nWarnings:")
        for warning in health['warnings']:
            print(f"  ⚠ {warning}")
    
    if not health['issues'] and not health['warnings']:
        print("\n✓ All systems operating normally")
    
    print("-" * 50 + "\n")
    
    print("=" * 50)
    print(f"Quick Check Complete: {health['overall_status'].upper()}")
    print("=" * 50)
    
    return {
        'health': health,
        'services': services,
        'errors': errors,
        'response_times': response_times,
        'queue': queue,
        'connections': connections,
        'log_status': log_status
    }


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    quick_check_flow(
        check_interval="30min",
        timeout=60
    )
