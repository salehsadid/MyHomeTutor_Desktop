# Test Script - MyHomeTutor Application
Write-Host "===== MyHomeTutor Application Test Script =====" -ForegroundColor Cyan

# Check Java
Write-Host "`n1. Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java is installed" -ForegroundColor Green
    Write-Host $javaVersion[0] -ForegroundColor Gray
} catch {
    Write-Host "✗ Java is NOT installed" -ForegroundColor Red
    Write-Host "Install from: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Yellow
}

# Check Maven
Write-Host "`n2. Checking Maven installation..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Maven is installed" -ForegroundColor Green
    Write-Host $mavenVersion -ForegroundColor Gray
} catch {
    Write-Host "✗ Maven is NOT installed" -ForegroundColor Red
    Write-Host "Install from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
}

# Check project structure
Write-Host "`n3. Checking project structure..." -ForegroundColor Yellow

$files = @(
    "pom.xml",
    "src\main\java\com\myhometutor\Main.java",
    "src\main\java\com\myhometutor\controller\HomeController.java",
    "src\main\java\com\myhometutor\controller\StudentRegisterController.java",
    "src\main\java\com\myhometutor\controller\TutorRegisterController.java",
    "src\main\java\com\myhometutor\controller\StudentDashboardController.java",
    "src\main\java\com\myhometutor\database\DatabaseManager.java",
    "src\main\java\com\myhometutor\model\SessionManager.java",
    "src\main\resources\fxml\HomePage.fxml",
    "src\main\resources\fxml\StudentRegister.fxml",
    "src\main\resources\fxml\TutorRegister.fxml",
    "src\main\resources\fxml\StudentDashboard.fxml",
    "src\main\resources\css\style.css"
)

$allFilesExist = $true
foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file (MISSING)" -ForegroundColor Red
        $allFilesExist = $false
    }
}

# Check images
Write-Host "`n4. Checking image files..." -ForegroundColor Yellow
if (Test-Path "src\main\resources\images\logo.png") {
    Write-Host "  ✓ logo.png exists" -ForegroundColor Green
} else {
    Write-Host "  ⚠ logo.png is missing (Place your logo here)" -ForegroundColor Yellow
}

if (Test-Path "src\main\resources\images\default-avatar.png") {
    Write-Host "  ✓ default-avatar.png exists" -ForegroundColor Green
} else {
    Write-Host "  ⚠ default-avatar.png is missing (Create a 150x150px avatar)" -ForegroundColor Yellow
}

# Summary
Write-Host "`n===== Test Summary =====" -ForegroundColor Cyan
if ($allFilesExist) {
    Write-Host "✓ All required files are present" -ForegroundColor Green
    Write-Host "`nYou can now run the application with:" -ForegroundColor Cyan
    Write-Host "  mvn clean javafx:run" -ForegroundColor White
} else {
    Write-Host "✗ Some files are missing. Please check the output above." -ForegroundColor Red
}

Write-Host "`n" -ForegroundColor White
