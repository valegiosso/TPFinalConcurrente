$zip = Join-Path $env:TEMP 'apache-maven.zip'
Write-Output "Descargando Apache Maven 3.8.8..."
Invoke-WebRequest 'https://downloads.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip' -OutFile $zip
Write-Output "Descomprimiendo..."
Expand-Archive -Path $zip -DestinationPath 'C:\' -Force
Write-Output "Actualizando variables de entorno de usuario..."
setx M2_HOME "C:\apache-maven-3.8.8"
setx PATH "%PATH%;C:\apache-maven-3.8.8\bin"
Write-Output "Instalación completada. Verificando mvn..."
C:\apache-maven-3.8.8\bin\mvn -v
