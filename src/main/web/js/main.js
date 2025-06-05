// Simulated database systems
class DatabaseSimulator {

  constructor() {
    this.currentDB = 'sql';
    this.fetchServerHealth().then();

    this.setupSampleData();
    this.setupEventListeners();
    this.updateUI();
  }

  async fetchServerHealth() {
    await fetch('http://localhost:8080/')
    .then(response => response.json())
    .then(response => {
      const data = response?.data ?? {
        connected: false,
        sampleQueries: [],
        tableNames: [],
      };
      this.updateStatusIndicator(data.connected);
      this.populateSampleQueries(data.sampleQueries);
      console.log('tableNames', data.tableNames);
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

  setupSampleData() {

    // NoSQL sample data
    this.nosqlData = {
      'user:1': {
        name: 'Alice Johnson',
        age: 16,
        profile: {
          interests: ['coding', 'gaming', 'music'],
          achievements: ['First Hackathon', 'Code Challenge Winner'],
          social: {
            friends: ['user:2', 'user:3'],
            posts: 15,
            likes: 127
          }
        }
      },
      'user:2': {
        name: 'Bob Smith',
        age: 15,
        profile: {
          interests: ['sports', 'coding', 'movies'],
          achievements: ['Team Captain', 'Honor Roll'],
          social: {
            friends: ['user:1', 'user:4'],
            posts: 8,
            likes: 64
          }
        }
      }
    };

    // Cache sample data
    this.cacheData = new Map();
    this.cacheData.set('session:user1', JSON.stringify(
        {userId: 1, loginTime: Date.now(), lastActivity: Date.now()}));
    this.cacheData.set('recent:posts',
        JSON.stringify(['post1', 'post2', 'post3']));
    this.cacheData.set('popular:games',
        JSON.stringify(['Minecraft', 'Roblox', 'Fortnite']));
  }

  setupEventListeners() {
    // Database tab switching
    document.querySelectorAll('.db-tab').forEach(tab => {
      tab.addEventListener('click', (e) => {
        this.switchDatabase(e.target.dataset.db);
      });
    });

    // Execute query button
    document.getElementById('execute-btn').addEventListener('click', () => {
      this.executeQuery();
    });

    // Sample query clicks
    document.addEventListener('click', (e) => {
      if (e.target.closest('.sample-query')) {
        const query = e.target.closest('.sample-query').dataset.query;
        document.getElementById('query-input').value = query;
      }
    });
  }

  switchDatabase(dbType) {
    this.currentDB = dbType;

    // Update tab appearances
    document.querySelectorAll('.db-tab').forEach(tab => {
      tab.classList.remove('active');
    });
    document.querySelector(`[data-db="${dbType}"]`).classList.add('active');

    this.updateUI();
  }

  updateUI() {
    const titleElement = document.getElementById('current-db-title');

    switch (this.currentDB) {
      case 'sql':
        titleElement.textContent = 'SQL Query Builder';
        break;
      case 'nosql':
        titleElement.textContent = 'NoSQL Document Store';
        this.populateSampleQueries([
          {title: 'Get user profile', query: 'user:1'},
          {title: 'Get another user', query: 'user:2'},
          {title: 'List all keys', query: 'KEYS *'},
          {
            title: 'Store new data',
            query: 'SET user:3 {"name": "Charlie", "age": 16}'
          }
        ]);
        break;
      case 'cache':
        titleElement.textContent = 'Cache Operations';
        this.populateSampleQueries([
          {title: 'Get session data', query: 'session:user1'},
          {title: 'Get recent posts', query: 'recent:posts'},
          {title: 'Get popular games', query: 'popular:games'},
          {title: 'Set cache value', query: 'SET temp:data "Hello Cache!"'}
        ]);
        break;
    }
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
    const query = document.getElementById('query-input').value.trim();
    const startTime = performance.now();

    // Show loading
    const resultsContent = document.getElementById('results-content');
    resultsContent.innerHTML = '<div class="loading"></div> Executing query...';

    // Simulate network delay
    setTimeout(() => {
      const endTime = performance.now();
      const executionTime = (endTime - startTime).toFixed(2);

      try {
        const result = this.processQuery(query);
        this.displayResults(result, executionTime);
      } catch (error) {
        this.displayError(error.message, executionTime);
      }
    }, Math.random() * 200 + 100); // Random delay between 100-300ms
  }

  processQuery(query) {
    switch (this.currentDB) {
      case 'sql':
        return this.processSQLQuery(query);
      case 'nosql':
        return this.processNoSQLQuery(query);
      case 'cache':
        return this.processCacheQuery(query);
      default:
        throw new Error('Unknown database type');
    }
  }

  async processSQLQuery(query) {
    console.log(query);

    await fetch('http://localhost:8080/api/query', {
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
    });

    // const upperQuery = query.toUpperCase().trim();
    //
    // if (upperQuery.includes('SELECT * FROM STUDENTS')) {
    //   return {
    //     type: 'table',
    //     data: this.sqlData.students,
    //     count: this.sqlData.students.length
    //   };
    // } else if (upperQuery.includes('SELECT * FROM CLASSES')) {
    //   return {
    //     type: 'table',
    //     data: this.sqlData.classes,
    //     count: this.sqlData.classes.length
    //   };
    // } else if (upperQuery.includes('COUNT(*)')) {
    //   return {
    //     type: 'scalar',
    //     value: this.sqlData.students.length,
    //     message: 'Total count'
    //   };
    // } else if (upperQuery.includes('WHERE GRADE = 11')) {
    //   const filtered = this.sqlData.students.filter(s => s.grade === 11);
    //   return {type: 'table', data: filtered, count: filtered.length};
    // } else if (upperQuery.includes('ORDER BY AGE DESC')) {
    //   const sorted = [...this.sqlData.students].sort((a, b) => b.age - a.age);
    //   return {type: 'table', data: sorted, count: sorted.length};
    // } else {
    //   throw new Error(
    //       'SQL query not recognized. Try one of the sample queries!');
    // }
  }

  processNoSQLQuery(query) {
    const trimmedQuery = query.trim();

    if (trimmedQuery.startsWith('SET ')) {
      return {type: 'success', message: 'Data stored successfully (simulated)'};
    } else if (trimmedQuery === 'KEYS *') {
      return {type: 'list', data: Object.keys(this.nosqlData)};
    } else if (this.nosqlData[trimmedQuery]) {
      return {type: 'document', data: this.nosqlData[trimmedQuery]};
    } else {
      throw new Error('Key not found in NoSQL store');
    }
  }

  processCacheQuery(query) {
    const trimmedQuery = query.trim();

    if (trimmedQuery.startsWith('SET ')) {
      return {
        type: 'success',
        message: 'Cache value set successfully (simulated)'
      };
    } else if (this.cacheData.has(trimmedQuery)) {
      const data = JSON.parse(this.cacheData.get(trimmedQuery));
      return {type: 'cache', data: data};
    } else {
      throw new Error('Cache key not found');
    }
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
      case 'document':
        resultsContent.innerHTML = this.formatJSONResults(result.data);
        dataVisualization.innerHTML = `
            <h4>📄 Document Info</h4>
            <p><strong>Document type:</strong> User Profile</p>
            <p><strong>Fields:</strong>${Object.keys(result.data).length}</p>
        `;
        break;
      case 'cache':
        resultsContent.innerHTML = this.formatJSONResults(result.data);
        dataVisualization.innerHTML = `
            <h4>⚡ Cache Performance</h4>
            <p><strong>Cache hit:</strong> ✅ Found in cache</p>
            <p><strong>Data type:</strong> ${typeof result.data}</p>
        `;
        break;
      case 'scalar':
        resultsContent.innerHTML = `
            <div class="json-view"><span class="json-number">${result.value}</span></div>
        `;
        dataVisualization.innerHTML = `
            <h4>📊 Query Result</h4>
            <p><strong>Result:</strong> ${result.message}</p>
            <p><strong>Value:</strong> ${result.value}</p>
        `;
        break;
      case 'success':
        resultsContent.innerHTML = `<div style="color: #10b981; font-weight: 600;">✅ ${result.message}</div>`;
        dataVisualization.innerHTML = `
            <h4>✅ Operation Status</h4>
            <p><strong>Status:</strong> Success</p>
        `;
        break;
      case 'list':
        resultsContent.innerHTML = `<div class="json-view">${result.data.map(
            item => `<div>"${item}"</div>`).join('')}</div>`;
        dataVisualization.innerHTML = `
            <h4>🔑 Keys Found</h4>
            <p><strong>Total keys:</strong> ${result.data.length}</p>
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
      columns.forEach((_,idx) => {
        html += `<td>${row[idx]}</td>`;
      });
      html += '</tr>';
    });
    html += '</tbody></table></div>';

    return html;
  }

  formatJSONResults(data) {
    return `<div class="json-view">${this.syntaxHighlightJSON(
        JSON.stringify(data, null, 2))}</div>`;
  }

  syntaxHighlightJSON(json) {
    return json
    .replace(
        /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
        function (match) {
          let cls = 'json-number';
          if (/^"/.test(match)) {
            if (/:$/.test(match)) {
              cls = 'json-key';
            } else {
              cls = 'json-string';
            }
          } else if (/true|false/.test(match)) {
            cls = 'json-boolean';
          } else if (/null/.test(match)) {
            cls = 'json-null';
          }
          return '<span class="' + cls + '">' + match + '</span>';
        });
  }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
  new DatabaseSimulator();
});