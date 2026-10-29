import java.sql.Connection

title='Stock full map and complete stock CSV export'
description='Public no-GUI orchestration matching the official Groovy tutorial'
inputs=[:]
outputs=[result:[name:'result',title:'result',type:String.class]]

def exec(Connection connection, Map input) {
    String stock=System.getenv('N5_STOCK')
    GroovyShell shell=new GroovyShell()
    Script noise=shell.parse(new File(stock+'/noisemodelling/wps/NoiseModelling/Noise_level_from_traffic.groovy'))
    noise.invokeMethod('exec',[connection,[tableBuilding:'BUILDINGS',tableRoads:'ROADS',tableReceivers:'RECEIVERS',confReflOrder:'1',confMaxSrcDist:'150',confMaxReflDist:'50',confThreadNumber:'1',confDiffVertical:true,confDiffHorizontal:true,confMaxError:'0',confExportSourceId:false]])
    Script exporter=shell.parse(new File(stock+'/noisemodelling/wps/Import_and_Export/Export_Table.groovy'))
    exporter.invokeMethod('exec',[connection,[tableToExport:'RECEIVERS_LEVEL',exportPath:new File('RECEIVERS_LEVEL.csv').absolutePath]])
    return 'Complete stock CSV exported'
}
