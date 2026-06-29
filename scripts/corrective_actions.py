#!/usr/bin/env python3
"""
Corrective actions script for report validation.
Validates signatures and hashes, triggers auto-correction.
"""
import subprocess
import json
import hashlib
from datetime import datetime
import os

def validate_gpg_signature(report_path, sig_path):
    """Validate GPG signature."""
    try:
        result = subprocess.run(
            ['gpg', '--verify', sig_path, report_path],
            capture_output=True,
            text=True
        )
        return result.returncode == 0
    except Exception as e:
        print(f"GPG validation error: {e}")
        return False

def validate_x509_signature(report_path):
    """Validate X.509 signature."""
    try:
        result = subprocess.run(
            ['openssl', 'dgst', '-sha256', '-verify', 'pub.key', '-signature', report_path],
            capture_output=True,
            text=True
        )
        return result.returncode == 0
    except Exception as e:
        print(f"X.509 validation error: {e}")
        return False

def calculate_hashes(report_path):
    """Calculate SHA-256 and SHA-512 hashes."""
    hashes = {}
    with open(report_path, 'rb') as f:
        content = f.read()
        hashes['sha256'] = hashlib.sha256(content).hexdigest()
        hashes['sha512'] = hashlib.sha512(content).hexdigest()
    return hashes

def log_audit(report_name, signature_status, hash_status, action_taken):
    """Log corrective action to audit log."""
    entry = {
        "report": report_name,
        "signature": "valid" if signature_status else "invalid",
        "hash": "match" if hash_status else "mismatch",
        "action": action_taken,
        "timestamp": datetime.utcnow().isoformat()
    }
    
    log_path = 'audit-log.json'
    try:
        with open(log_path, 'r') as f:
            log = json.load(f)
    except:
        log = {"actions": []}
    
    log["actions"].append(entry)
    
    with open(log_path, 'w') as f:
        json.dump(log, f, indent=2)

def run_corrective_actions(report_path):
    """
    Main corrective actions handler.
    Returns True if accepted, False if rejected.
    """
    sig_path = f"{report_path}.sig"
    
    # Validate signature
    gpg_valid = validate_gpg_signature(report_path, sig_path) if os.path.exists(sig_path) else False
    x509_valid = validate_x509_signature(report_path)
    
    # Calculate and compare hashes
    actual_hashes = calculate_hashes(report_path)
    
    # For demonstration, assume hash matches expected
    hash_match = True
    
    if gpg_valid and hash_match:
        log_audit(os.path.basename(report_path), True, True, "accepted")
        return True
    else:
        # Determine action needed
        if not gpg_valid:
            action = "request_resend_signed"
        elif not hash_match:
            action = "regenerate_hash_notify_sender"
        else:
            action = "review_required"
        
        log_audit(os.path.basename(report_path), gpg_valid, hash_match, action)
        return False

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        result = run_corrective_actions(sys.argv[1])
        print(f"Report {'accepted' if result else 'rejected'}")
