def parametersFile = new File('parameters.yaml')
def parameters = parametersFile.text

def parametersConfig = evaluate(parameters)

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

if(env.INSTANCE == "Please select one of the options in LINE"){
  print "Please select one of the options in LINE"
  currentBuild.result = 'FAILURE'
}
else{
  print "catbusRestart has been initiated!!"
  catbusRestart(channel)
}
