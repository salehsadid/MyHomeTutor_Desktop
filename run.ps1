# Run MyHomeTutor Application
Write-Host "Starting MyHomeTutor Application..." -ForegroundColor Cyan

# Check if Maven is installed
if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

# Check if Java is installed
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install JDK 11 or higher from: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Yellow
    exit 1
}

Write-Host "Java version:" -ForegroundColor Green
java -version

Write-Host "`nMaven version:" -ForegroundColor Green
mvn -version

Write-Host "`nBuilding and running application..." -ForegroundColor Cyan
mvn clean javafx:run
