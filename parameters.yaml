parameters:
  - $class: 'ChoiceParameter'
    choiceType: 'PT_SINGLE_SELECT'
    name: 'LINE'
    description: 'Select the LINE you want to restart'
    script:
      $class: 'GroovyScript'
      script: |
        return ["Prod_NP", "Prod_Mgmt", "Prod_QA", "Prod_Spint", "E1_NP", "E1_Mgmt"]
  - $class: 'CascadeChoiceParameter'
    choiceType: 'PT_SINGLE_SELECT'
    name: 'INSTANCE'
    description: 'Select the INSTANCE you want to restart'
    referencedParameters: 'LINE'
    script:
      $class: 'GroovyScript'
      script: |
        def LINE_VALUE = LINE ?: "Prod_NP"
        switch (LINE_VALUE) {
          case "Prod_NP":
            return ["catbus-usa.int.p1-np-usw2-01", "catbus-europe.int.p1-np-usw2-01", "catbus-japan.int.p1-np-usw2-01", "catbus-video.int.p1-np-usw2-01", "catbus-diffcollector.int.p1-np-usw2-01"]
          case "Prod_Mgmt":
            return ["catbus-mgmt.int.mgmt-usw2-01", "catbus-mgmt-video.int.mgmt-usw2-01"]
          case "Prod_QA":
            return ["catbus-pqa.int.pqa-usw2-01"]
          case "Prod_Spint":
            return ["catbus-sp-int.int.sp-int-usw2-01"]
          case "E1_NP":
            return ["catbus-e1-np.int.e1-usw2-01"]
          case "E1_Mgmt":
            return ["catbus-e1-mgmt.int.e1-usw2-01"]
          default:
            return []
        }
