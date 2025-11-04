# Step-by-Step Setup Guide

This guide will walk you through setting up your Prefect project from scratch on Windows.

## Step 1: Extract the Project

1. Extract the zip file to a location on your computer
   - Example: `C:\Users\YourName\prefect-flows`
   - Avoid paths with spaces if possible

2. Open Command Prompt or PowerShell
   ```cmd
   cd C:\Users\YourName\prefect-flows
   ```

## Step 2: Verify Python Installation

```cmd
python --version
```

Should show Python 3.8 or higher. If not installed:
- Download from https://www.python.org/downloads/
- Install with "Add Python to PATH" checked

## Step 3: Create Virtual Environment (Recommended)

```cmd
# Create virtual environment
python -m venv venv

# Activate it
venv\Scripts\activate

# You should see (venv) in your prompt
```

## Step 4: Install Prefect and Dependencies

```cmd
# Upgrade pip first
python -m pip install --upgrade pip

# Install all dependencies
pip install -r requirements.txt

# Verify Prefect installation
prefect version
```

Expected output: `3.4.24` or similar

## Step 5: Start Prefect Server (First Time)

```cmd
# Start the Prefect server in a new terminal/command prompt
prefect server start
```

This will:
- Start the Prefect API server
- Open the Prefect UI in your browser at http://127.0.0.1:4200
- Keep this terminal window open

**Note:** You can also use Prefect Cloud instead of local server.

## Step 6: Update prefect.yaml Paths

Open `prefect.yaml` in a text editor and update the path:

**Before:**
```yaml
pull:
  - prefect.deployments.steps.set_working_directory:
      directory: "C:/Users/YourName/prefect-flows"
```

**After (use your actual path):**
```yaml
pull:
  - prefect.deployments.steps.set_working_directory:
      directory: "C:/Users/ActualUsername/actual-folder-name"
```

**Important:** 
- Use forward slashes `/` or double backslashes `\\`
- Can use `"."` for current directory

## Step 7: Test a Flow Locally

Open a **new** terminal (keep the server running in the first one):

```cmd
# Navigate to project folder
cd C:\Users\YourName\prefect-flows

# Activate virtual environment if you created one
venv\Scripts\activate

# Test a simple flow
python flows\etl_flow.py
```

You should see output showing the flow running.

## Step 8: Create Work Pool

In your terminal:

```cmd
prefect work-pool create default-process-pool --type process
```

Verify it was created:
```cmd
prefect work-pool ls
```

You should see `default-process-pool` in the list.

## Step 9: Deploy Your Flows

```cmd
# Deploy all flows at once
prefect deploy --all
```

This will:
- Register all 8 deployments from prefect.yaml
- Set up their schedules
- Make them ready to run

Verify deployments:
```cmd
prefect deployment ls
```

## Step 10: Start a Worker

In a **new** terminal window:

```cmd
# Navigate to project folder
cd C:\Users\YourName\prefect-flows

# Activate virtual environment if you created one
venv\Scripts\activate

# Start the worker
prefect worker start --pool default-process-pool
```

Keep this terminal window open. The worker will:
- Poll for scheduled flows
- Execute flows when triggered
- Show logs in real-time

## Step 11: Verify Everything is Working

### Check the Prefect UI:

1. Open http://127.0.0.1:4200 in your browser
2. Click on "Deployments" - you should see your 8 deployments
3. Click on "Work Pools" - you should see `default-process-pool`
4. Click on "Workers" - you should see your worker online

### Run a Test Deployment:

```cmd
# Manually trigger a flow run
prefect deployment run "etl-pipeline/daily-etl-job"
```

Watch the worker terminal - you should see it pick up and execute the flow.

## Step 12: Monitor Your First Run

1. In the Prefect UI, click on "Flow Runs"
2. You should see your test run
3. Click on it to see detailed logs
4. Check that it completed successfully

## Your Setup is Complete! 🎉

You now have:
- ✅ Prefect server running
- ✅ 8 deployments configured
- ✅ Work pool created
- ✅ Worker running and ready to execute flows
- ✅ Schedules active

## Terminal Window Summary

You should have **3 terminal windows open**:

1. **Terminal 1:** Prefect Server
   ```cmd
   prefect server start
   ```

2. **Terminal 2:** Worker
   ```cmd
   prefect worker start --pool default-process-pool
   ```

3. **Terminal 3:** For running commands
   ```cmd
   # Use this for deploying, testing, etc.
   ```

## What Happens Next?

Your flows will now run automatically based on their schedules:
- `daily-etl-job` - Every day at 2 AM
- `hourly-data-sync` - Every hour
- `weekly-report` - Every Monday at 9 AM
- `business-hours-monitor` - 4 times per day on weekdays
- `monthly-cleanup` - First day of each month at 3 AM
- `frequent-check` - Every 30 minutes
- `complex-schedule-flow` - Custom schedule
- `manual-data-import` - Only when you trigger it manually

## Common Issues and Solutions

### Issue: Port 4200 already in use
**Solution:** 
```cmd
# Kill existing Prefect server
taskkill /F /IM prefect.exe
# Then restart: prefect server start
```

### Issue: Worker not starting
**Solution:**
- Make sure work pool exists: `prefect work-pool ls`
- Check for typos in work pool name
- Ensure you're in the correct directory

### Issue: Flows not running on schedule
**Solution:**
- Verify worker is running
- Check deployment schedules: `prefect deployment ls`
- Ensure deployments are not paused

### Issue: Import errors when flow runs
**Solution:**
- Make sure requirements.txt is installed in the same environment
- Check that the working directory path is correct in prefect.yaml

## Optional: Running as Windows Service

To run Prefect components as Windows services (so they start automatically):

1. Install NSSM (Non-Sucking Service Manager)
2. Create services for:
   - Prefect Server
   - Prefect Worker

See: https://docs.prefect.io/latest/guides/deployment/daemonize/

## Optional: Using Prefect Cloud

Instead of running a local server, you can use Prefect Cloud:

1. Sign up at https://app.prefect.cloud
2. Get your API key
3. Set API key:
   ```cmd
   prefect cloud login
   ```
4. No need to run `prefect server start`
5. Everything else remains the same

## Next Steps

1. **Customize the flows** - Edit files in `flows/` directory
2. **Add your own logic** - Replace placeholder code with real operations
3. **Set up notifications** - Get alerts when flows fail
4. **Add more deployments** - Follow the pattern in prefect.yaml
5. **Explore Prefect UI** - Discover features like flow run history, logs, etc.

## Need Help?

- Check README.md for troubleshooting tips
- Visit https://docs.prefect.io for documentation
- Ask questions at https://discourse.prefect.io

---

**Congratulations!** You're now ready to build and automate workflows with Prefect! 🚀
