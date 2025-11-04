"""
Complex Schedule Flow
This flow demonstrates advanced scheduling with RRule.
Schedule: 10 AM, 2 PM, and 4 PM on weekdays only
"""

from prefect import flow, task
from datetime import datetime
import time


@task
def check_business_hours():
    """
    Verify we're running during business hours.
    
    Returns:
        dict: Business hours check result
    """
    now = datetime.now()
    day_of_week = now.weekday()  # 0=Monday, 6=Sunday
    hour = now.hour
    
    is_weekday = day_of_week < 5  # Monday-Friday
    is_business_hours = 8 <= hour < 18  # 8 AM to 6 PM
    
    result = {
        'current_time': now.isoformat(),
        'day_of_week': ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'][day_of_week],
        'is_weekday': is_weekday,
        'is_business_hours': is_business_hours,
        'should_run': is_weekday and is_business_hours
    }
    
    print(f"[{now}] Business hours check:")
    print(f"  Day: {result['day_of_week']}")
    print(f"  Time: {now.strftime('%I:%M %p')}")
    print(f"  Weekday: {result['is_weekday']}")
    print(f"  Business hours: {result['is_business_hours']}")
    
    return result


@task
def get_scheduled_tasks(mode: str):
    """
    Retrieve tasks scheduled for this time slot.
    
    Args:
        mode: Operating mode (production, staging, etc.)
        
    Returns:
        list: List of tasks to execute
    """
    print(f"[{datetime.now()}] Retrieving scheduled tasks for {mode} mode...")
    
    # In production, this would query a task scheduler or database
    # to determine what work needs to be done at this time
    
    tasks = [
        {'name': 'data_validation', 'priority': 'high', 'estimated_duration': 5},
        {'name': 'cache_refresh', 'priority': 'medium', 'estimated_duration': 3},
        {'name': 'report_update', 'priority': 'low', 'estimated_duration': 8},
        {'name': 'metrics_collection', 'priority': 'high', 'estimated_duration': 2}
    ]
    
    print(f"✓ Found {len(tasks)} tasks to execute")
    return tasks


@task(retries=2, retry_delay_seconds=30)
def execute_task(task_info: dict, mode: str, max_retries: int):
    """
    Execute a scheduled task.
    
    Args:
        task_info: Information about the task
        mode: Operating mode
        max_retries: Maximum number of retries
        
    Returns:
        dict: Task execution result
    """
    print(f"[{datetime.now()}] Executing task: {task_info['name']} (Priority: {task_info['priority']})")
    
    # Simulate task execution
    time.sleep(task_info['estimated_duration'] * 0.1)  # Scaled down for demo
    
    result = {
        'task_name': task_info['name'],
        'status': 'success',
        'duration_seconds': task_info['estimated_duration'],
        'mode': mode,
        'timestamp': datetime.now().isoformat()
    }
    
    print(f"✓ Task '{task_info['name']}' completed in {result['duration_seconds']}s")
    return result


@task
def prioritize_tasks(tasks: list):
    """
    Sort tasks by priority.
    
    Args:
        tasks: List of tasks
        
    Returns:
        list: Prioritized task list
    """
    print(f"[{datetime.now()}] Prioritizing tasks...")
    
    priority_order = {'high': 0, 'medium': 1, 'low': 2}
    prioritized = sorted(tasks, key=lambda t: priority_order.get(t['priority'], 3))
    
    print("✓ Task execution order:")
    for i, task in enumerate(prioritized, 1):
        print(f"  {i}. {task['name']} - {task['priority']} priority")
    
    return prioritized


@task
def aggregate_results(task_results: list):
    """
    Aggregate results from all executed tasks.
    
    Args:
        task_results: List of task execution results
        
    Returns:
        dict: Aggregated summary
    """
    print(f"[{datetime.now()}] Aggregating results...")
    
    total_duration = sum(r['duration_seconds'] for r in task_results)
    successful = sum(1 for r in task_results if r['status'] == 'success')
    failed = sum(1 for r in task_results if r['status'] != 'success')
    
    summary = {
        'total_tasks': len(task_results),
        'successful': successful,
        'failed': failed,
        'total_duration_seconds': total_duration,
        'average_duration': round(total_duration / len(task_results), 2) if task_results else 0,
        'tasks': [r['task_name'] for r in task_results]
    }
    
    print(f"✓ Summary: {summary['successful']}/{summary['total_tasks']} tasks successful")
    print(f"  Total duration: {summary['total_duration_seconds']}s")
    
    return summary


@task
def send_execution_summary(summary: dict, business_hours: dict):
    """
    Send execution summary to monitoring system.
    
    Args:
        summary: Aggregated results
        business_hours: Business hours check result
        
    Returns:
        str: Confirmation message
    """
    print(f"[{datetime.now()}] Sending execution summary...")
    
    # In production, this would send to a monitoring dashboard,
    # log aggregation system, or notification service
    
    report = f"""
    Execution Summary - {business_hours['day_of_week']} {business_hours['current_time']}
    
    Tasks Executed: {summary['total_tasks']}
    Successful: {summary['successful']}
    Failed: {summary['failed']}
    Total Duration: {summary['total_duration_seconds']}s
    Average Duration: {summary['average_duration']}s
    
    Tasks: {', '.join(summary['tasks'])}
    """
    
    print("✓ Summary sent to monitoring system")
    return "Summary sent successfully"


@flow(name="complex-schedule-flow", log_prints=True)
def complex_flow(mode: str = "production", max_retries: int = 3):
    """
    Complex scheduled flow that runs at specific times on weekdays.
    
    This flow checks business hours, retrieves scheduled tasks,
    executes them in priority order, and reports results.
    
    Args:
        mode: Operating mode (production, staging, development)
        max_retries: Maximum number of retries for failed tasks
        
    Returns:
        dict: Flow execution summary
    """
    print("=" * 50)
    print(f"Starting Complex Scheduled Flow")
    print(f"Mode: {mode}")
    print("=" * 50)
    
    # Check if we should be running
    business_hours = check_business_hours()
    
    if not business_hours['should_run']:
        print("⚠ Not during business hours - flow should not have run")
        print("  This indicates a scheduling configuration issue")
    
    # Get scheduled tasks
    tasks = get_scheduled_tasks(mode)
    
    # Prioritize tasks
    prioritized_tasks = prioritize_tasks(tasks)
    
    # Execute all tasks
    task_results = []
    for task_info in prioritized_tasks:
        result = execute_task(task_info, mode, max_retries)
        task_results.append(result)
    
    # Aggregate results
    summary = aggregate_results(task_results)
    
    # Send summary
    send_status = send_execution_summary(summary, business_hours)
    
    print("=" * 50)
    print(f"Flow Complete: {summary['successful']}/{summary['total_tasks']} tasks successful")
    print(f"Status: {send_status}")
    print("=" * 50)
    
    return {
        'business_hours_check': business_hours,
        'summary': summary,
        'send_status': send_status,
        'mode': mode
    }


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    print("Testing complex_flow...")
    print("Note: This flow is designed to run at 10 AM, 2 PM, and 4 PM on weekdays")
    print("")
    
    complex_flow(
        mode="production",
        max_retries=3
    )
