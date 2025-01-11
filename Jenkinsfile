pipeline {
    agent any
    environment {
        GIT_HOME = '/usr/bin/git' // Beispiel für den Git-Pfad
        DEPLOY_PATH = '/opt/cms_vx'  // Zielverzeichnis auf deinem Server
        SERVICE_NAME = 'PR_CMS.service'      // Der Name des Systemdienstes
    }
    stages {
        stage('Checkout') {
            steps {
                // Git-Repository klonen
                git branch: 'main', url: 'https://github.com/Lucashlfr/PR_CMS_VX.git'
            }
        }
        stage('Build') {
            steps {
                // Maven-Build durchführen
                sh 'mvn clean install'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // Kopiere die gebaute JAR-Datei in das Zielverzeichnis
                    sh """
                    cp target/*.jar ${DEPLOY_PATH}/
                    """

                    // Service starten oder neu starten
                    sh """
                    sudo systemctl stop ${SERVICE_NAME}
                    sudo systemctl start ${SERVICE_NAME}
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Build and deployment succeeded!'
        }
        failure {
            echo 'Build or deployment failed.'
        }
    }
}
