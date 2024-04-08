def parametersContent = readFile('/path/to/parameters.yaml')

// Alternatively, if you prefer shell commands:
// def parametersContent = sh(script: 'cat /path/to/parameters.yaml', returnStdout: true).trim()

def parametersConfig = evaluate(parametersContent)

properties([
  buildDiscarder(
    logRotator(
      artifactDaysToKeepStr: '',
      artifactNumToKeepStr: '',
      daysToKeepStr: '',
      numToKeepStr: '1000'
    )
  ),
  parametersConfig['parameters']
])

def channel = "#kmj-jenkins-updates"

currentBuild.description = "CATBUS RESTART: ${INSTANCE}"

if (env.INSTANCE == "Please select one of the options in LINE") {
  print "Please select one of the options in LINE"
  currentBuild.result = 'FAILURE'
} else {
  print "catbusRestart has been initiated!!"
  catbusRestart(channel)
}
