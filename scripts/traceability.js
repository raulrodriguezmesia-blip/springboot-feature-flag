const fs = require('fs');
const path = require('path');

// Log para trazabilidad de distribución
const DISTRIBUTION_LOG = path.join(__dirname, '..', 'distribution-log.json');

/**
 * Track email open event
 */
async function track_open(eventId) {
    const entry = {
        report: eventId,
        opened: true,
        opened_at: new Date().toISOString(),
        downloaded: false
    };
    log_distribution(entry);
    return entry;
}

/**
 * Track file download event
 */
async function track_download(fileId) {
    const entry = {
        report: fileId,
        opened: true,
        downloaded: true,
        downloaded_at: new Date().toISOString()
    };
    log_distribution(entry);
    return entry;
}

/**
 * Save to distribution log
 */
function log_distribution(entry) {
    let log = [];
    try {
        const data = fs.readFileSync(DISTRIBUTION_LOG, 'utf8');
        log = JSON.parse(data);
    } catch (err) {
        // File doesn't exist, start fresh
    }
    
    log.push(entry);
    fs.writeFileSync(DISTRIBUTION_LOG, JSON.stringify(log, null, 2));
}

module.exports = { track_open, track_download };

// Example usage with Microsoft Graph API would integrate here:
// async function fetch_graph_events() {
//   // GET /me/events with $filter=contains(subject,'Corrective Report')
// }
