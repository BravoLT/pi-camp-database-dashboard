#!/bin/bash

# SQL Learning App Setup Script for Raspberry Pi
# This script sets up the Java application with H2 database

echo "🚀 Setting up SQL Learning Application on Raspberry Pi..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if Java is installed
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    JAVA_MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)

    # Check if Java version is 17 or higher
    if [ "$JAVA_MAJOR_VERSION" -ge 17 ] 2>/dev/null; then
        print_status "Java found: $JAVA_VERSION (compatible)"
    else
        print_warning "Java $JAVA_VERSION found, but Java 17+ required. Installing OpenJDK 17..."
        sudo apt update
        sudo apt install -y openjdk-17-jdk

        # Set Java 17 as default if multiple versions exist
        sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-*/bin/java
        sudo update-alternatives --set javac /usr/lib/jvm/java-17-openjdk-*/bin/javac
        print_status "Java 17 installed and set as default"
    fi
else
    print_error "Java not found. Installing OpenJDK 17..."
    sudo apt update
    sudo apt install -y openjdk-17-jdk
    print_status "Java 17 installed successfully"
fi

# Verify Java installation
FINAL_JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
print_status "Active Java version: $FINAL_JAVA_VERSION"

# Check if Maven is installed
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version | head -n1)
    print_status "Maven found: $MAVEN_VERSION"
else
    print_error "Maven not found. Installing Maven..."
    sudo apt update
    sudo apt install -y maven
    print_status "Maven installed successfully"
fi

# Create project directory
PROJECT_DIR="$HOME/sql-learning-app"
echo "Creating project directory: $PROJECT_DIR"

if [ -d "$PROJECT_DIR" ]; then
    print_warning "Directory already exists. Backing up..."
    mv "$PROJECT_DIR" "${PROJECT_DIR}.backup.$(date +%Y%m%d_%H%M%S)"
fi

mkdir -p "$PROJECT_DIR/src/main/java"
cd "$PROJECT_DIR"

print_status "Project directory created"

# Create the Java source file
echo "Creating Java application..."
cat > src/main/java/SQLLearningApp.java << 'EOF'
// Note: The Java code from the first artifact should be placed here
// This is just a placeholder - copy the complete SQLLearningApp.java content here
EOF

print_status "Java source file created"

# Create Maven pom.xml
echo "Creating Maven configuration..."
cat > pom.xml << 'EOF'
# Note: The pom.xml content from the second artifact should be placed here
EOF

print_status "Maven configuration created"

# Create start script
echo "Creating start script..."
cat > start-sql-app.sh << 'EOF'
#!/bin/bash
echo "🎓 Starting SQL Learning Application..."
echo "========================================="
echo "This will start:"
echo "1. H2 Database with sample data"
echo "2. H2 Web Console at http://localhost:8082"
echo "3. Interactive SQL console"
echo ""
echo "Students can access the H2 console in their browser at:"
echo "http://localhost:8082"
echo ""
echo "Database connection details:"
echo "JDBC URL: jdbc:h2:~/sqllearning;AUTO_SERVER=TRUE"
echo "Username: student"
echo "Password: learn123"
echo ""
echo "Press Ctrl+C to stop the application"
echo "========================================="

cd "$(dirname "$0")"
java -jar target/sql-learning-app-1.0.0-shaded.jar
EOF

chmod +x start-sql-app.sh
print_status "Start script created"

# Create quick reference guide
echo "Creating student reference guide..."
cat > SQL_QUICK_REFERENCE.md << 'EOF'
# SQL Quick Reference for Students

## Getting Started
1. Run the application: `./start-sql-app.sh`
2. Open H2 Console in browser: http://localhost:8082
3. Use the interactive console that appears in terminal

## Database Connection (for H2 Console)
- **JDBC URL**: `jdbc:h2:~/sqllearning;AUTO_SERVER=TRUE`
- **Username**: `student`
- **Password**: `learn123`

## Available Tables
- **students**: Student information (id, name, age, grade, email)
- **books**: Book catalog (id, title, author, genre, price, publication_year)
- **orders**: Student book orders (id, student_id, book_id, order_date, quantity)

## Basic SQL Commands

### SELECT (Read Data)
```sql
-- Get all students
SELECT * FROM students;

-- Get specific columns
SELECT name, age FROM students;

-- Filter with WHERE
SELECT * FROM students WHERE age > 14;

-- Sort results
SELECT * FROM students ORDER BY age;
```

### INSERT (Add Data)
```sql
-- Add a new student
INSERT INTO students (name, age, grade, email)
VALUES ('Your Name', 15, 10, 'you@school.edu');
```

### UPDATE (Modify Data)
```sql
-- Update student email
UPDATE students SET email = 'new@email.com' WHERE id = 1;
```

### DELETE (Remove Data)
```sql
-- Delete a student (be careful!)
DELETE FROM students WHERE id = 1;
```

### Advanced Queries
```sql
-- Count students by grade
SELECT grade, COUNT(*) as student_count
FROM students
GROUP BY grade;

-- Join tables to see student orders
SELECT s.name, b.title, o.order_date
FROM students s
JOIN orders o ON s.id = o.student_id
JOIN books b ON o.book_id = b.id;

-- Find most expensive books
SELECT title, price
FROM books
ORDER BY price DESC
LIMIT 3;
```

## Practice Exercises
1. Find all students older than 15
2. List books cheaper than $20
3. Count how many orders each student has made
4. Find the average book price by genre
5. List all Science Fiction books
6. Add yourself as a new student
7. Create an order for yourself

## Tips
- Always end SQL statements with semicolon (;)
- Use UPPER or lower case for SQL keywords
- Be careful with DELETE and UPDATE - always use WHERE!
- Try the examples in both H2 Console and terminal
EOF

print_status "Student reference guide created"

# Build the application
echo "Building the application..."
mvn clean package

if [ $? -eq 0 ]; then
    print_status "Application built successfully!"
else
    print_error "Build failed. Please check for errors above."
    exit 1
fi

# Create desktop shortcut (optional)
echo "Creating desktop shortcut..."
cat > "$HOME/Desktop/SQL Learning App.desktop" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=SQL Learning App
Comment=Learn SQL with H2 Database
Exec=$PROJECT_DIR/start-sql-app.sh
Icon=applications-education
Terminal=true
Categories=Education;Development;
EOF

chmod +x "$HOME/Desktop/SQL Learning App.desktop"
print_status "Desktop shortcut created"

# Final instructions
echo ""
echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo "To start the application:"
echo "1. Navigate to: $PROJECT_DIR"
echo "2. Run: ./start-sql-app.sh"
echo "3. Or double-click the desktop shortcut"
echo ""
echo "Students can then:"
echo "• Use the interactive console in terminal"
echo "• Open browser to http://localhost:8082 for H2 Console"
echo "• Reference SQL_QUICK_REFERENCE.md for help"
echo ""
echo "Files created in: $PROJECT_DIR"
print_status "Ready for SQL learning!"
EOF