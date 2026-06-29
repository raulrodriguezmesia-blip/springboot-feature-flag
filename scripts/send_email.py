#!/usr/bin/env python3
"""
Email distribution script for executive reports.
Supports SMTP and Microsoft Graph API.
"""
import smtplib
import json
from email.mime.multipart import MIMEMultipart
from email.mime.application import MIMEApplication
from datetime import datetime
import os

def send_email(recipients, subject, body, attachments=None, use_graph=False):
    """
    Send email with optional attachments.
    
    Args:
        recipients: List of email addresses
        subject: Email subject
        body: Email body text
        attachments: List of file paths to attach
        use_graph: Use Microsoft Graph API instead of SMTP
    """
    if use_graph:
        # Microsoft Graph API integration would go here
        # POST https://graph.microsoft.com/v1.0/me/sendMail
        print(f"Sending via Microsoft Graph to {recipients}")
    else:
        # SMTP configuration from environment
        smtp_server = os.getenv('SMTP_SERVER', 'smtp.office365.com')
        smtp_port = int(os.getenv('SMTP_PORT', 587))
        smtp_user = os.getenv('SMTP_USER')
        smtp_pass = os.getenv('SMTP_PASS')
        
        msg = MIMEMultipart()
        msg['Subject'] = subject
        msg['From'] = smtp_user
        msg['To'] = ', '.join(recipients)
        
        # Attach body
        msg.attach(MIMEText(body, 'plain'))
        
        # Attach files
        for attachment in attachments or []:
            with open(attachment, 'rb') as f:
                part = MIMEApplication(f.read(), Name=os.path.basename(attachment))
            part['Content-Disposition'] = f'attachment; filename="{os.path.basename(attachment)}"'
            msg.attach(part)
        
        # Send email
        try:
            server = smtplib.SMTP(smtp_server, smtp_port)
            server.starttls()
            server.login(smtp_user, smtp_pass)
            server.send_message(msg)
            server.quit()
            log_audit(subject, recipients, attachments)
            return True
        except Exception as e:
            print(f"Email send failed: {e}")
            return False

def log_audit(subject, recipients, attachments):
    """Log email distribution to audit log."""
    audit_entry = {
        "report": subject,
        "recipients": recipients,
        "attachments": attachments or [],
        "timestamp": datetime.utcnow().isoformat(),
        "status": "sent"
    }
    
    log_path = 'audit-log.json'
    try:
        with open(log_path, 'r') as f:
            log = json.load(f)
    except:
        log = {"reports": []}
    
    log["reports"].append(audit_entry)
    
    with open(log_path, 'w') as f:
        json.dump(log, f, indent=2)

if __name__ == '__main__':
    # Example usage
    send_email(
        recipients=['ceo@company.com'],
        subject='Corrective Report Q2 2026',
        body='Please find attached the quarterly corrective report.',
        attachments=['reports/corrective-report-Q2-2026-CEO.pdf']
    )
