$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
& "C:\Users\Prashant Ashok Ghuge\.tools\apache-maven-3.9.6\bin\mvn.cmd" @args
