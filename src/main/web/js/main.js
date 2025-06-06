class DbManager {

  constructor() {
    this.fetchServerHealth().then();
    this.setupEventListeners();
  }

  async fetchServerHealth() {
    await fetch('http://localhost:8080/sql/health')
    .then(response => response.json())
    .then(response => {
      const data = response?.data ?? {
        connected: false,
        sampleQueries: [],
        tableNames: [],
      };
      this.updateStatusIndicator(data.connected);
      this.populateSampleQueries(data.sampleQueries);
    })
    .catch(err => {
      this.updateStatusIndicator(false);
      console.log("error", err);
    });
  }

  updateStatusIndicator(isConnected) {
    document.querySelectorAll('.status-indicator').forEach(indicator => {
      const connectedClass = 'status-connected';
      const disconnectedClass = 'status-disconnected';
      if (isConnected) {
        indicator.classList.add(connectedClass);
        indicator.classList.remove(disconnectedClass);
      } else {
        indicator.classList.remove(connectedClass);
        indicator.classList.add(disconnectedClass);
      }
    });
  }

  setupEventListeners() {
    // Execute query button
    document.getElementById('execute-btn').addEventListener('click', () => {
      this.executeQuery();
    });

    // Execute database button
    document.getElementById('db-btn').addEventListener('click', () => {
      window.open('http://localhost:8082', '_blank');
    });

    // Sample query clicks
    document.addEventListener('click', (e) => {
      const closestQuery = e.target.closest('.sample-query');
      if (closestQuery) {
        document.getElementById('query-input').value =
            closestQuery.dataset.query;
      }
    });
  }

  populateSampleQueries(queries) {
    const queryInput = document.getElementById('query-input');
    queryInput.value = queries?.[0]?.query ?? 'Enter your query...';

    const container = document.getElementById('sample-queries-list');
    container.innerHTML = '';

    queries.forEach(query => {
      const div = document.createElement('div');
      div.className = 'sample-query';
      div.dataset.query = query.query;
      div.innerHTML = `
          <div class="sample-query-title">${query.title}</div>
          <div class="sample-query-code">${query.query}</div>
      `;
      container.appendChild(div);
    });
  }

  executeQuery() {
    // Show loading
    const resultsContent = document.getElementById('results-content');
    resultsContent.innerHTML = '<div class="loading"></div> Executing query...';

    const query = document.getElementById('query-input').value.trim();
    this.processSQLQuery(query).then();
  }

  async processSQLQuery(query) {
    await fetch('http://localhost:8080/sql/query', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        'sql': query
      })
    })
    .then(response => response.json())
    .then(response => {
      const data = response?.data ?? {
        type: 'ERROR',
        data: {
          columns: [],
          rows: [],
        },
        execTimeMs: 0
      };
      this.displayResults(response, data.execTimeMs);
    })
    .catch(err => this.displayError(err.message, 0));
  }

  displayResults(result, executionTime) {
    const resultsContent = document.getElementById('results-content');
    const performanceBadge = document.getElementById('performance-badge');
    const dataVisualization = document.getElementById('data-visualization');

    performanceBadge.textContent = `⚡ ${executionTime}ms`;

    switch (result.type) {
      case 'TABLE':
        resultsContent.innerHTML = this.formatTableResults(result.data);
        dataVisualization.innerHTML = `
            <h4>📈 Data Statistics</h4>
            <p><strong>Records returned:</strong> ${result.data.count}</p>
        `;
        break;
    }
  }

  displayError(message, executionTime) {
    const resultsContent = document.getElementById('results-content');
    const performanceBadge = document.getElementById('performance-badge');
    const dataVisualization = document.getElementById('data-visualization');

    performanceBadge.textContent = `❌ ${executionTime}ms`;
    resultsContent.innerHTML = `<div style="color: #ef4444; font-weight: 600;">❌ Error: ${message}</div>`;
    dataVisualization.innerHTML = `
        <h4>❌ Query Error</h4>
        <p><strong>Status:</strong> Failed</p>
        <p><strong>Tip:</strong> Try one of the sample queries to get started!</p>
    `;
  }

  formatTableResults(data) {
    if (!data || data.length === 0) {
      return '<p>No results found.</p>';
    }

    const {
      columns,
      rows
    } = data;

    let html = '<div class="table-view"><table>';

    // Headers
    html += '<thead><tr>';
    columns.forEach(col => {
      html += `<th>${col}</th>`;
    });
    html += '</tr></thead>';

    // Data rows
    html += '<tbody>';
    rows.forEach(row => {
      html += '<tr>';
      columns.forEach((_, idx) => {
        html += `<td>${row[idx]}</td>`;
      });
      html += '</tr>';
    });
    html += '</tbody></table></div>';

    return html;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  new DbManager();
});