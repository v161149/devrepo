# Prefect Multi-Flow Project Example

This package contains a complete example of a Prefect 3.4.24 project with multiple flows, local storage configuration, and various scheduling options.

## 📁 Project Structure

```
prefect-project-example/
├── README.md                    # This file - complete instructions
├── SETUP_GUIDE.md              # Step-by-step setup guide
├── prefect.yaml                # Main deployment configuration
├── requirements.txt            # Python dependencies
├── flows/                      # Flow code directory
│   ├── etl_flow.py            # Daily ETL pipeline
│   ├── sync_flow.py           # Hourly database sync
│   ├── report_flow.py         # Weekly report generation
│   ├── monitor_flow.py        # Business hours monitoring
│   ├── cleanup_flow.py        # Monthly cleanup
│   ├── import_flow.py         # Manual data import
│   ├── quick_check.py         # Frequent system checks
│   └── complex_flow.py        # Complex schedule example
└── examples/
    ├── test_flows.py          # Script to test all flows locally
    └── deployment_examples.md # Additional deployment examples

```

## 🚀 Quick Start

### 1. Prerequisites

- Windows computer
- Python 3.8 or higher
- Prefect 3.4.24 installed (`pip install prefect==3.4.24`)

### 2. Installation

```bash
# Extract the zip file to your desired location
# For example: C:\Users\YourName\prefect-flows

# Navigate to the project directory
cd C:\Users\YourName\prefect-flows

# Install dependencies
pip install -r requirements.txt
```

### 3. Update Configuration

Edit `prefect.yaml` and update the storage path:

```yaml
pull:
  - prefect.deployments.steps.set_working_directory:
      directory: "C:/Users/YourName/prefect-flows"  # Update this path
```

### 4. Test Flows Locally (Optional)

```bash
# Test individual flow
python flows/etl_flow.py

# Or run the test script
python examples/test_flows.py
```

### 5. Create Work Pool

```bash
# Create a process work pool
prefect work-pool create default-process-pool --type process
```

### 6. Deploy Flows

```bash
# Deploy all flows at once
prefect deploy --all

# Or deploy specific flow
prefect deploy --name daily-etl-job
```

### 7. Start Worker

```bash
# Start a worker to execute the flows
prefect worker start --pool default-process-pool
```

## 📊 Deployment Overview

| Deployment Name | Schedule | Work Queue | Description |
|----------------|----------|------------|-------------|
| daily-etl-job | Daily at 2 AM | production | ETL pipeline |
| hourly-data-sync | Every hour | production | Database sync |
| weekly-report | Monday 9 AM | reports | Weekly report |
| business-hours-monitor | 4x daily (weekdays) | monitoring | System health check |
| monthly-cleanup | 1st of month 3 AM | maintenance | File cleanup |
| manual-data-import | No schedule | adhoc | Manual only |
| frequent-check | Every 30 minutes | production | Quick checks |
| complex-schedule-flow | Custom times | production | Advanced scheduling |

## 🔧 Common Commands

### View Deployments
```bash
prefect deployment ls
```

### View Work Pools
```bash
prefect work-pool ls
```

### Run a Flow Manually
```bash
prefect deployment run 'etl-pipeline/daily-etl-job'
```

### View Flow Runs
```bash
prefect flow-run ls
```

### Pause/Resume Schedule
```bash
# Pause
prefect deployment pause daily-etl-job

# Resume
prefect deployment resume daily-etl-job
```

## 🛠️ Customization

### Adding a New Flow

1. Create your flow file in the `flows/` directory:
```python
# flows/my_new_flow.py
from prefect import flow, task

@task
def my_task():
    print("Hello from my task!")

@flow(name="my-new-flow")
def my_new_flow():
    my_task()
```

2. Add deployment configuration to `prefect.yaml`:
```yaml
  - name: my-new-deployment
    entrypoint: flows/my_new_flow.py:my_new_flow
    work_pool:
      name: default-process-pool
      work_queue_name: production
      job_variables:
        type: process
    schedule:
      cron: "0 10 * * *"  # Daily at 10 AM
      timezone: "America/New_York"
```

3. Deploy:
```bash
prefect deploy --name my-new-deployment
```

### Schedule Examples

**Cron Schedule:**
```yaml
schedule:
  cron: "0 9 * * *"      # Daily at 9 AM
  timezone: "UTC"
```

**Interval Schedule:**
```yaml
schedule:
  interval: 3600          # Every hour (seconds)
  anchor_date: "2024-01-01T00:00:00"
```

**RRule Schedule:**
```yaml
schedule:
  rrule: "FREQ=WEEKLY;BYDAY=MO,FR;BYHOUR=9"  # Monday and Friday at 9 AM
```

**No Schedule (Manual):**
```yaml
schedule: null
```

## 🔍 Troubleshooting

### Issue: "Remote storage prompt appears"
**Solution:** Make sure `prefect.yaml` has the `pull` section configured and use `--no-prompt` flag.

### Issue: "Schedule prompt appears"
**Solution:** Add `schedule: null` or a specific schedule to each deployment in `prefect.yaml`.

### Issue: "Worker not picking up flows"
**Solution:** 
- Verify work pool name matches in `prefect.yaml`
- Check worker is running: `prefect worker ls`
- Restart worker if needed

### Issue: "Module not found error"
**Solution:** 
- Ensure all dependencies are installed: `pip install -r requirements.txt`
- Verify the working directory path in `prefect.yaml` is correct
- Check that flow files exist in the `flows/` directory

### Issue: "Flow runs but fails immediately"
**Solution:**
- Check flow logs in Prefect UI or CLI
- Test flow locally first: `python flows/your_flow.py`
- Verify all file paths are correct for Windows (use forward slashes or double backslashes)

## 📚 Additional Resources

- **Prefect Documentation:** https://docs.prefect.io
- **Prefect Community:** https://discourse.prefect.io
- **Prefect Cloud:** https://app.prefect.cloud (optional hosted service)

## 💡 Tips

1. **Always test flows locally** before deploying
2. **Use parameters** to make flows flexible and reusable
3. **Monitor flow runs** through Prefect UI at http://127.0.0.1:4200
4. **Use tags** to organize deployments: add `tags: ["production", "etl"]` to deployments
5. **Set up notifications** for failed flows using Prefect blocks
6. **Use retries** on tasks that might fail temporarily
7. **Cache task results** when appropriate to avoid redundant work

## 🎯 Next Steps

1. ✅ Extract and review all files
2. ✅ Update paths in `prefect.yaml`
3. ✅ Install dependencies
4. ✅ Test flows locally
5. ✅ Create work pool
6. ✅ Deploy flows
7. ✅ Start worker
8. ✅ Monitor executions in Prefect UI

## 📞 Support

For questions about Prefect:
- Documentation: https://docs.prefect.io
- Community Forum: https://discourse.prefect.io
- GitHub: https://github.com/PrefectHQ/prefect

---

**Version:** 1.0  
**Compatible with:** Prefect 3.4.24  
**Last Updated:** November 2025
