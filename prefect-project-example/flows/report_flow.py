"""
Report Generation Flow
This flow generates and sends weekly reports.
Schedule: Every Monday at 9 AM
"""

from prefect import flow, task
from datetime import datetime, timedelta
import pandas as pd


@task
def gather_weekly_data(report_type: str):
    """
    Gather data for the weekly report.
    
    Args:
        report_type: Type of report to generate
        
    Returns:
        dict: Collected data for the report
    """
    print(f"[{datetime.now()}] Gathering data for {report_type} report...")
    
    # Simulated data gathering - replace with actual database queries
    # In production, this would query your data warehouse or databases
    
    # Calculate date range for the past week
    end_date = datetime.now()
    start_date = end_date - timedelta(days=7)
    
    data = {
        'report_period': f"{start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}",
        'metrics': {
            'total_sales': 125000,
            'total_orders': 450,
            'average_order_value': 277.78,
            'new_customers': 85,
            'returning_customers': 365
        },
        'top_products': [
            {'name': 'Product A', 'units': 120, 'revenue': 12000},
            {'name': 'Product B', 'units': 95, 'revenue': 19000},
            {'name': 'Product C', 'units': 78, 'revenue': 11700}
        ],
        'regional_breakdown': {
            'North': 42000,
            'South': 38000,
            'East': 25000,
            'West': 20000
        }
    }
    
    print(f"✓ Data gathered for period: {data['report_period']}")
    return data


@task
def analyze_trends(data: dict):
    """
    Analyze trends and generate insights.
    
    Args:
        data: Raw report data
        
    Returns:
        dict: Analysis results with trends and insights
    """
    print(f"[{datetime.now()}] Analyzing trends...")
    
    # Perform analysis - compare with previous periods, identify trends
    analysis = {
        'growth_rate': 12.5,  # 12.5% growth vs previous week
        'trend': 'increasing',
        'insights': [
            'Sales increased 12.5% compared to previous week',
            'Product B showing strongest growth (+25%)',
            'North region outperforming others',
            'Customer retention rate: 81%'
        ],
        'recommendations': [
            'Increase inventory for Product B',
            'Focus marketing efforts on South region',
            'Launch loyalty program to boost retention'
        ]
    }
    
    print(f"✓ Analysis complete: {len(analysis['insights'])} insights generated")
    return analysis


@task
def create_report(data: dict, analysis: dict, report_type: str):
    """
    Create the formatted report document.
    
    Args:
        data: Report data
        analysis: Analysis results
        report_type: Type of report
        
    Returns:
        str: Report content
    """
    print(f"[{datetime.now()}] Creating {report_type} report...")
    
    # Create formatted report
    report = f"""
    ==========================================
    WEEKLY BUSINESS REPORT
    ==========================================
    
    Report Type: {report_type}
    Period: {data['report_period']}
    Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
    
    ------------------------------------------
    KEY METRICS
    ------------------------------------------
    Total Sales: ${data['metrics']['total_sales']:,.2f}
    Total Orders: {data['metrics']['total_orders']}
    Avg Order Value: ${data['metrics']['average_order_value']:.2f}
    New Customers: {data['metrics']['new_customers']}
    Returning Customers: {data['metrics']['returning_customers']}
    
    Growth Rate: {analysis['growth_rate']}%
    Trend: {analysis['trend'].upper()}
    
    ------------------------------------------
    TOP PRODUCTS
    ------------------------------------------
    """
    
    for i, product in enumerate(data['top_products'], 1):
        report += f"\n    {i}. {product['name']}: {product['units']} units, ${product['revenue']:,.2f}"
    
    report += "\n\n    ------------------------------------------"
    report += "\n    REGIONAL BREAKDOWN"
    report += "\n    ------------------------------------------\n"
    
    for region, revenue in data['regional_breakdown'].items():
        report += f"    {region}: ${revenue:,.2f}\n"
    
    report += "\n    ------------------------------------------"
    report += "\n    KEY INSIGHTS"
    report += "\n    ------------------------------------------\n"
    
    for i, insight in enumerate(analysis['insights'], 1):
        report += f"    {i}. {insight}\n"
    
    report += "\n    ------------------------------------------"
    report += "\n    RECOMMENDATIONS"
    report += "\n    ------------------------------------------\n"
    
    for i, rec in enumerate(analysis['recommendations'], 1):
        report += f"    {i}. {rec}\n"
    
    report += "\n    =========================================="
    
    print("✓ Report created successfully")
    return report


@task(retries=2, retry_delay_seconds=30)
def send_email(report: str, recipients: list):
    """
    Send the report via email.
    
    Args:
        report: The report content
        recipients: List of email addresses
        
    Returns:
        str: Confirmation message
    """
    print(f"[{datetime.now()}] Sending report to {len(recipients)} recipients...")
    
    # Simulated email sending - replace with actual email logic
    # import smtplib
    # from email.mime.text import MIMEText
    # ...
    
    print(f"  Recipients: {', '.join(recipients)}")
    print("✓ Email sent successfully")
    
    return f"Report sent to {len(recipients)} recipients"


@flow(name="report-generator", log_prints=True)
def generate_report(report_type: str, recipients: list):
    """
    Generate and send a weekly business report.
    
    This flow gathers data, performs analysis, creates a formatted report,
    and sends it to specified recipients.
    
    Args:
        report_type: Type of report to generate
        recipients: List of email addresses to send the report to
        
    Returns:
        str: Summary of report generation and delivery
    """
    print("=" * 50)
    print(f"Starting Report Generation: {report_type}")
    print("=" * 50)
    
    # Gather data
    data = gather_weekly_data(report_type)
    
    # Analyze trends
    analysis = analyze_trends(data)
    
    # Create report
    report = create_report(data, analysis, report_type)
    
    # Print report to console for verification
    print("\n" + report + "\n")
    
    # Send email
    send_result = send_email(report, recipients)
    
    print("=" * 50)
    print(f"Report Generation Complete: {send_result}")
    print("=" * 50)
    
    return send_result


# Allow running the flow directly for testing
if __name__ == "__main__":
    # Test the flow locally
    generate_report(
        report_type="weekly_summary",
        recipients=["manager@company.com", "team@company.com"]
    )
