pipeline {
    agent any
    
    stages {
        stage('Load Parameters') {
            steps {
                script {
                    def params = load 'parameters.groovy'
                }
            }
        }
        stage('Read Parameters') {
            steps {
                script {
                    echo "Parameter: ${params.ENVIRONMENT_LINE}"
                }
            }
        }
    }
}
