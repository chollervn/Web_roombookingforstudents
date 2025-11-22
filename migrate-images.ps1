# PowerShell script to migrate images from static/img to uploads directory

$projectDir = "c:\Users\Admin\Downloads\PTIT\Nam_3\Code\OOP\BTL\OOP_BTL"
$staticImgDir = "$projectDir\src\main\resources\static\img"
$uploadsDir = "$projectDir\uploads"

# Create uploads directories
$dirs = @("room_img", "profile_img")
foreach ($dir in $dirs) {
    $targetDir = "$uploadsDir\$dir"
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        Write-Host "Created: $targetDir"
    }
}

# Copy room images
if (Test-Path "$staticImgDir\room_img") {
    Copy-Item -Path "$staticImgDir\room_img\*" -Destination "$uploadsDir\room_img\" -Force -ErrorAction SilentlyContinue
    Write-Host "Copied room images"
}

# Copy profile images
if (Test-Path "$staticImgDir\profile_img") {
    Copy-Item -Path "$staticImgDir\profile_img\*" -Destination "$uploadsDir\profile_img\" -Force -ErrorAction SilentlyContinue
    Write-Host "Copied profile images"
}

# Copy default.jpg
if (Test-Path "$staticImgDir\default.jpg") {
    Copy-Item -Path "$staticImgDir\default.jpg" -Destination "$uploadsDir\profile_img\" -Force
    Write-Host "Copied default.jpg"
}

Write-Host "✅ Migration complete!"
