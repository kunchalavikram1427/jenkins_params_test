pipeline {
    agent any
    
    stages {
        stage('Load Parameters') {
            steps {
                script {
                    def params = load 'parameters.groovy'
                    // Access parameters as needed
                    echo "Parameter: ${params.ENVIRONMENT_LINE}"
                }
            }
        }
        // Add more stages as needed
    }
}
