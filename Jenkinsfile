pipeline {
    agent any
    stages{
        stage('init'){
            script{


def setProperties(){
    // def orgList = ["SIE", "SIE-PRIVATE"]
    def domainList = ["", "coreexperience", "dataplatform", "gaminglife", "monetization", "partnersplatform", "pci", "platformfundamentals", "psdirect"]
    // def infrastructureList = ["", "kamaji-cloud", "laco-cloud", "navigator-cloud", "roadster-cloud"]
    def pillarList = ["", "Roadster", "Kamaji", "Laco", "Navigator"]
    def testList = ["", "Enable post deployment test"]
    def serviceCriticaity = ["", "Low", "Medium", "High"]
    def enableTestList = "\'"+testList.join("\', \'")+"\'"
    
    def settings = [
        choice(
                choices: domainList.join("\n"),
                description: 'Required: PSN Domain name, i.e.: coreexperience',
                name: 'PSN_DOMAIN'
        ),
        choice(
                choices: infrastructureList.join("\n"),
                description: 'Required: infrastructure name, i.e.: navigator-cloud',
                name: 'INFRASTRUCTURE'
        ),
        choice(
                choices: ["", "k8s-service", "k8s-cron"].join("\n"),
                description: 'Optional: For batch deployment, please select k8s-cron',
                name: 'WORKLOAD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Github repository name, i.e.: poki-simple',
                name: 'REPO_NAME'
        ),
        string(
                defaultValue: '',
                description: 'Required: GitHub branch with Unified Chart files i.e.: main',
                name: 'REPO_BRANCH'
        ),
        string(
                defaultValue: '',
                description: 'Required: Folder path in git repo of unified Chart File, i.e.: impl/helm-unified/poki-simple',
                name: 'HELM_CHART_PATH'
        ),
        choice(
                choices: orgList.join("\n"),
                description: 'Required: Github organization name, i.e.: SIE',
                name: 'ORG_NAME'
        ),
        string(
                defaultValue: '',
                description: "Optional: helm chart name and the default value is repo name, i.e.: poki-simple",
                name: 'CHART_NAME'
        ),
        string(
                defaultValue: '',
                description: 'Required: the prefix of k8s namesplace on the global level, i.e. if the namespace is poki-simple-e1np, the namespacePrefix will be poki-simple',
                name: 'NAMESPACE_PREFIX'
        ),
        string(
                defaultValue: '',
                description: "Optional: helm release name like 'helm install HELM_RELEASE_NAME ...', if it is not defined, the repo name will be used. i.e.: poki-simple",
                name: 'HELM_RELEASE_NAME'
        ),
        booleanParam(
            defaultValue: false,
            description: 'Optional: Support helm deployment from github',
            name: 'DEPLOY_FROM_GITHUB'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Aloy groups to view the CD jobs [use comma to seperate groups], i.e.: PSN-CoreJenkinsProd-SSECare',
                name: 'ALOY_GROUPS_TO_VIEW'
        ),
        string(
                defaultValue: '',
                description: 'Required: Aloy groups with Jenkins jobs view, build and has nonprod deployment permissions [use comma to seperate groups], i.e.: PSN-CoreJenkinsNprd-EngEnable',
                name: 'ALOY_GROUPS_TO_DEPLOY_NONPROD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Aloy groups with Jenkins jobs view, build, configure, and has prod and nonprod deployment permissions [use comma to seperate groups], i.e.: PSN-CoreJenkinsNprd-EngEnable',
                name: 'ALOY_GROUPS_TO_DEPLOY_PROD'
        ),
        string(
                defaultValue: '',
                description: 'Required: Primary Contact for this Service - service owner, or team leads email - used for ServiceNow Onboarding, i.e.: jun.yu@sony.com',
                name: 'PRIMARY_CONTACT'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Secondary Contact for this Service - service owner, or team leads email - used for ServiceNow Onboarding, i.e.: gerry.soulos@sony.com',
                name: 'SECONDARY_CONTACT'
        ),
        string(
                defaultValue: '',
                description: 'Required: Enter XMATTER Group, which will be used to contact service team in case of emergency. - used for ServiceNow Onboarding, i.e.: gerry.soulos@sony.com',
                name: 'XMATTERS_GROUP'
        ),
        string(
                defaultValue: '',
                description: 'Required: Description, purpose, or function of this Service. - used for Service Now Onboarding, i.e.: poki-simple is a deployment testapp',
                name: 'SERVICE_DESCRIPTION'
        ),        
        choice(
                choices: pillarList.join("\n"),
                description: 'Optional: If team used previous ServiceNow, what pillar? - used for Service Now Onboarding, i.e.: Roadster, Kamaji, Laco, Navigator',
                name: 'SERVICENOW_LEGACY_PILLAR'
        ),
        choice(
                choices: serviceCriticaity.join("\n"),
                description: 'Required: How would a service outage impact the PlayStation network? - used for Service Now Onboarding , i.e.: Low',
                name: 'SERVICE_CRITICALITY'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Slack team channels [use comma to separate channels], i.e.: poki-simple',
                name: 'SLACK_CHANNELS'
        ),
        string(
                defaultValue: '',
                description: 'Optional: Pair with serviceName and serviceNowCIName, format: serviceName:serviceNowCIName [use comma to separate config names] . i.e. Haste_Sample:Haste_Sample-psn',
                name: 'SERVICENOW_NAMES'
        ),
        booleanParam(
            defaultValue: false,
            description: 'Applicable for LACO repo only and if checked: RAA is ready to scan.',
            name: 'RAA_ENABLED'
        )
    ]

    def kmjDeployList = ["", "E1-PMGT", "E1-NP", "MGMT_PQA_SPINT", "P1-NP"]
    def psenvList = "\'"+kmjDeployList.join("\', \'")+"\'"
    settings.add([$class: 'CascadeChoiceParameter',
        name: 'AUTO_CD_DEPLOY_UPTO',
        description: 'KAMAJI CLOUD ONLY: the last environment to deploy to: i.e. E1-NP',
        choiceType: 'PT_SINGLE_SELECT',
        filterLength: 1,
        filterable: false,
        randomName: 'choice-parameter-01',
        referencedParameters: 'INFRASTRUCTURE',
        script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: true, script: "return [' NOT APPLICABLE ']"],
                script: [classpath: [], sandbox: true, script: """
                if (INFRASTRUCTURE.equals("kamaji-cloud")) {
                    return [${psenvList}]
                } else {
                    return ['NOT APPLICABLE']
                }"""]
        ]
    ])

    def kmjEnvList = "\'"+createCDJob.getAllPsenvList("kamaji-cloud").join("\', \'")+"\'"
    def lacoEnvList = "\'"+createCDJob.getAllPsenvList("laco-cloud").join("\', \'")+"\'"
    def navEnvList = "\'"+createCDJob.getAllPsenvList("navigator-cloud").join("\', \'")+"\'"
    def rdsEnvList = "\'"+createCDJob.getAllPsenvList("roadster-cloud").join("\', \'")+"\'"
    settings.add([$class: 'CascadeChoiceParameter',
        name: 'DEPLOY_ENVS',
        description: 'Required: deployment line + envs, i.e.: e1-np, p1-np',
        choiceType: 'PT_CHECKBOX',
        filterLength: 1,
        filterable: false,
        randomName: 'choice-parameter-02',
        referencedParameters: 'INFRASTRUCTURE',
        script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: true, script: "return [' NOT APPLICABLE ']"],
                script: [classpath: [], sandbox: true, script: """
                if (INFRASTRUCTURE.equals("kamaji-cloud")) {
                    return [${kmjEnvList}]
                } else if (INFRASTRUCTURE.equals("laco-cloud")) {
                    return [${lacoEnvList}]
                } else if (INFRASTRUCTURE.equals("navigator-cloud")) {
                    return [${navEnvList}]
                } else if (INFRASTRUCTURE.equals("roadster-cloud")) {
                    return [${rdsEnvList}]
                } else {
                    return ['NOT APPLICABLE']
                }"""]
        ]
    ])
     
    properties([
        buildDiscarder(
            logRotator(
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                daysToKeepStr: '30',
                numToKeepStr: '1000')
        ),
        parameters(settings),
        pipelineTriggers([])
    ])
}

            }
        }
    }
}

// // def parametersContent = readFile('/path/to/parameters.yaml')

// def parametersContent = sh(script: 'cat /path/to/parameters.yaml', returnStdout: true).trim()

// def parametersConfig = evaluate(parametersContent)

// properties([
//   buildDiscarder(
//     logRotator(
//       artifactDaysToKeepStr: '',
//       artifactNumToKeepStr: '',
//       daysToKeepStr: '',
//       numToKeepStr: '1000'
//     )
//   ),
//   parametersConfig['parameters']
// ])

// def channel = "#kmj-jenkins-updates"

// currentBuild.description = "CATBUS RESTART: ${INSTANCE}"

// if (env.INSTANCE == "Please select one of the options in LINE") {
//   print "Please select one of the options in LINE"
//   currentBuild.result = 'FAILURE'
// } else {
//   print "catbusRestart has been initiated!!"
//   catbusRestart(channel)
// }
