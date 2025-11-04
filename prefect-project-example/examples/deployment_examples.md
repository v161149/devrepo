# Deployment Examples and Advanced Configuration

This document provides additional examples and advanced configuration options for Prefect deployments.

## Table of Contents
1. [Additional Schedule Examples](#additional-schedule-examples)
2. [Parameters and Variables](#parameters-and-variables)
3. [Tags and Organization](#tags-and-organization)
4. [Multiple Work Pools](#multiple-work-pools)
5. [Environment-Specific Deployments](#environment-specific-deployments)
6. [Advanced Flow Patterns](#advanced-flow-patterns)

---

## Additional Schedule Examples

### Daily at Multiple Times
```yaml
schedule:
  cron: "0 8,12,16,20 * * *"  # 8 AM, 12 PM, 4 PM, 8 PM daily
  timezone: "America/New_York"
```

### Every X Hours During Business Hours
```yaml
schedule:
  cron: "0 9-17 * * MON-FRI"  # Every hour from 9 AM to 5 PM on weekdays
  timezone: "America/Chicago"
```

### First and Last Day of Month
```yaml
schedule:
  cron: "0 6 1,L * *"  # 1st and last day of month at 6 AM
  timezone: "UTC"
```

### Quarterly Schedule
```yaml
schedule:
  cron: "0 9 1 1,4,7,10 *"  # 9 AM on Jan 1, Apr 1, Jul 1, Oct 1
  timezone: "America/Los_Angeles"
```

### Every X Minutes Between Times
```yaml
schedule:
  rrule: "FREQ=MINUTELY;INTERVAL=15;BYHOUR=9,10,11,12,13,14,15,16,17"
  timezone: "UTC"
# Runs every 15 minutes between 9 AM and 6 PM
```

### Complex Weekly Pattern
```yaml
schedule:
  rrule: "FREQ=WEEKLY;BYDAY=MO,WE,FR;BYHOUR=10,14"
  timezone: "America/New_York"
# Runs Monday, Wednesday, Friday at 10 AM and 2 PM
```

---

## Parameters and Variables

### Using Dynamic Parameters
```yaml
deployments:
  - name: dynamic-etl
    entrypoint: flows/etl_flow.py:etl_flow
    work_pool:
      name: default-process-pool
    parameters:
      api_url: "{{ prefect.variables.api_url }}"  # Reference a Prefect variable
      output_path: "{{ prefect.variables.output_path }}"
      environment: "production"
```

Create variables via CLI:
```bash
prefect variable set api_url "https://api.production.com/data"
prefect variable set output_path "C:/data/production"
```

### Using Blocks for Configuration
```yaml
deployments:
  - name: etl-with-blocks
    entrypoint: flows/etl_flow.py:etl_flow
    work_pool:
      name: default-process-pool
    parameters:
      database: "{{ prefect.blocks.database-credentials.prod-db }}"
      storage: "{{ prefect.blocks.s3-bucket.data-bucket }}"
```

### Environment-Based Parameters
```yaml
deployments:
  - name: etl-dev
    entrypoint: flows/etl_flow.py:etl_flow
    parameters:
      api_url: "https://api.dev.com/data"
      environment: "development"
      debug: true
  
  - name: etl-prod
    entrypoint: flows/etl_flow.py:etl_flow
    parameters:
      api_url: "https://api.prod.com/data"
      environment: "production"
      debug: false
```

---

## Tags and Organization

### Using Tags for Filtering
```yaml
deployments:
  - name: critical-etl
    entrypoint: flows/etl_flow.py:etl_flow
    tags: ["critical", "production", "etl", "high-priority"]
  
  - name: dev-test
    entrypoint: flows/test_flow.py:test_flow
    tags: ["development", "testing", "low-priority"]
```

Filter deployments by tag:
```bash
# List all critical deployments
prefect deployment ls --tag critical

# Run all ETL deployments
prefect deployment run --tag etl
```

---

## Multiple Work Pools

### Different Work Pools for Different Tasks
```yaml
deployments:
  # CPU-intensive tasks
  - name: data-processing
    entrypoint: flows/heavy_flow.py:process_data
    work_pool:
      name: high-cpu-pool
      work_queue_name: intensive
  
  # I/O-intensive tasks
  - name: file-sync
    entrypoint: flows/sync_flow.py:sync_flow
    work_pool:
      name: io-pool
      work_queue_name: file-operations
  
  # GPU tasks
  - name: ml-training
    entrypoint: flows/ml_flow.py:train_model
    work_pool:
      name: gpu-pool
      work_queue_name: ml-tasks
```

Create work pools:
```bash
prefect work-pool create high-cpu-pool --type process
prefect work-pool create io-pool --type process
prefect work-pool create gpu-pool --type process
```

Start specialized workers:
```bash
# Terminal 1: CPU worker
prefect worker start --pool high-cpu-pool

# Terminal 2: I/O worker
prefect worker start --pool io-pool

# Terminal 3: GPU worker
prefect worker start --pool gpu-pool
```

---

## Environment-Specific Deployments

### Development, Staging, Production
```yaml
name: multi-environment-project

pull:
  - prefect.deployments.steps.set_working_directory:
      directory: "."

deployments:
  # Development
  - name: etl-dev
    entrypoint: flows/etl_flow.py:etl_flow
    work_pool:
      name: dev-pool
    schedule: null  # Manual only in dev
    parameters:
      environment: "development"
      api_url: "https://api.dev.company.com"
      log_level: "DEBUG"
    tags: ["dev", "etl"]
  
  # Staging
  - name: etl-staging
    entrypoint: flows/etl_flow.py:etl_flow
    work_pool:
      name: staging-pool
    schedule:
      cron: "0 */4 * * *"  # Every 4 hours
      timezone: "UTC"
    parameters:
      environment: "staging"
      api_url: "https://api.staging.company.com"
      log_level: "INFO"
    tags: ["staging", "etl"]
  
  # Production
  - name: etl-prod
    entrypoint: flows/etl_flow.py:etl_flow
    work_pool:
      name: prod-pool
    schedule:
      cron: "0 2 * * *"  # Daily at 2 AM
      timezone: "America/New_York"
    parameters:
      environment: "production"
      api_url: "https://api.company.com"
      log_level: "WARNING"
    tags: ["production", "etl", "critical"]
```

---

## Advanced Flow Patterns

### Parameterized Flow with Validation
```python
from prefect import flow
from pydantic import BaseModel, validator

class ETLConfig(BaseModel):
    api_url: str
    batch_size: int = 1000
    max_retries: int = 3
    
    @validator('batch_size')
    def batch_size_must_be_positive(cls, v):
        if v <= 0:
            raise ValueError('batch_size must be positive')
        return v

@flow
def validated_etl(config: ETLConfig):
    # Config is automatically validated
    print(f"Running ETL with batch size: {config.batch_size}")
```

### Conditional Execution Based on Parameters
```python
from prefect import flow, task

@task
def dev_only_task():
    print("This only runs in development")

@task
def prod_only_task():
    print("This only runs in production")

@flow
def environment_aware_flow(environment: str):
    if environment == "development":
        dev_only_task()
    elif environment == "production":
        prod_only_task()
```

### Flow with Dynamic Task Generation
```python
from prefect import flow, task

@task
def process_item(item: dict):
    print(f"Processing {item['name']}")
    return item

@flow
def dynamic_flow(items: list[dict]):
    results = []
    for item in items:
        result = process_item.submit(item)  # Parallel execution
        results.append(result)
    
    # Wait for all to complete
    return [r.result() for r in results]
```

---

## Useful Commands Reference

### Deployment Management
```bash
# List all deployments
prefect deployment ls

# Get details of a specific deployment
prefect deployment inspect "flow-name/deployment-name"

# Pause a deployment schedule
prefect deployment pause deployment-name

# Resume a deployment schedule
prefect deployment resume deployment-name

# Delete a deployment
prefect deployment delete deployment-name

# Update deployment parameters
prefect deployment set-schedule "flow-name/deployment-name" \
    --cron "0 10 * * *" \
    --timezone "America/New_York"
```

### Work Pool Management
```bash
# List work pools
prefect work-pool ls

# Get work pool details
prefect work-pool inspect pool-name

# Pause a work pool
prefect work-pool pause pool-name

# Resume a work pool
prefect work-pool resume pool-name

# Delete a work pool
prefect work-pool delete pool-name
```

### Flow Run Management
```bash
# List recent flow runs
prefect flow-run ls --limit 20

# Get details of a flow run
prefect flow-run inspect <flow-run-id>

# Cancel a flow run
prefect flow-run cancel <flow-run-id>

# Manually trigger a deployment
prefect deployment run "flow-name/deployment-name"

# Trigger with custom parameters
prefect deployment run "flow-name/deployment-name" \
    --param api_url="https://custom.api.com" \
    --param environment="test"
```

### Worker Management
```bash
# List active workers
prefect worker ls

# Start a worker with custom settings
prefect worker start \
    --pool my-pool \
    --name my-worker \
    --limit 5  # Max concurrent flows

# Start worker with specific work queue
prefect worker start \
    --pool my-pool \
    --work-queue priority-queue
```

---

## Tips and Best Practices

1. **Use descriptive deployment names**: Include environment, purpose, and frequency
   - Good: `etl-prod-daily`, `report-staging-weekly`
   - Bad: `deployment1`, `test`

2. **Tag everything**: Use tags for filtering and organization
   - Environment: `dev`, `staging`, `prod`
   - Priority: `critical`, `high`, `low`
   - Type: `etl`, `report`, `monitoring`

3. **Separate work queues**: Use different queues for different priorities
   - `critical`: Highest priority, immediate execution
   - `production`: Normal production workloads
   - `reports`: Lower priority, can wait
   - `adhoc`: Manual runs, lowest priority

4. **Use parameters wisely**: Keep flows flexible with parameters
   - Avoid hardcoding values in flows
   - Use Prefect variables for configuration
   - Use blocks for credentials and connections

5. **Monitor and alert**: Set up notifications for failures
   ```python
   from prefect import flow
   from prefect.blocks.notifications import SlackWebhook
   
   @flow(on_failure=[slack_webhook.notify])
   def monitored_flow():
       # Your flow logic
       pass
   ```

6. **Test locally first**: Always test flows before deploying
   ```bash
   python flows/your_flow.py
   ```

7. **Use dry runs**: For destructive operations, support a `dry_run` parameter

8. **Version your flows**: Include version info in tags or parameters
   ```yaml
   tags: ["v1.2.3", "production"]
   ```

---

For more information, visit:
- Prefect Documentation: https://docs.prefect.io
- Deployment Guide: https://docs.prefect.io/concepts/deployments/
- Schedules: https://docs.prefect.io/concepts/schedules/
