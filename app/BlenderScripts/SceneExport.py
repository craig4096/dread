
##############################################################
# Blender v2.70a
##############################################################

bl_info = {
    "name":  "Acorn Scene Exporter",
    "author":   "Craig Hamilton",
    "blender":  (2, 7, 0),
    "version":  (0, 0, 1),
    "location":  "File > Import-Export",
    "description":  "Export Acorn mesh file",
    "category": "Import-Export"
}

PI = 3.14159265

import bpy
import os
import struct
import math
import mathutils
from bpy_extras.io_utils import ExportHelper

gSectorSize = 0.3
gSpawnSizeXY = 0.3
gSpawnSizeZ = 0.7

##########################################################################################
### Method to temporarily hide all custom Acorn objects in the scene
##########################################################################################
def HideAllAcornObjects():
    for object in bpy.data.objects:
        if object.acorn_type != "none":
            object.layers = (False,True,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False)

##########################################################################################
### Method to set visible all custom Acorn objects in the scene
##########################################################################################
def ShowAllAcornObjects():
    for object in bpy.data.objects:
        if object.acorn_type != "none":
            object.layers = (True,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False,False)


##########################################################################################
### Switches to the export camera
##########################################################################################

def SwitchToExportCamera():
    cam = bpy.data.objects["ExportCamera"]
    if cam is not None:
        bpy.data.scenes["Scene"].camera = cam
        return True
    return False
    

##########################################################################################
### Helpful class for formatting output to json files
##########################################################################################
class JSONWriter(object):

#private:

    def writeTabs(self):
        for i in range(0, self.tabCount):
            self.file.write("\t")

    def removeLastChar(self):
        pos = self.file.tell()
        self.file.seek(pos - 1, 0)
        self.file.write(" ")

    def newline(self):
        self.file.write("\n")

    def inctab(self):
        self.tabCount = self.tabCount + 1

    def dectab(self):
        self.tabCount = self.tabCount - 1

#public:

    def __init__(self, filepath):
        self.tabCount = 0
        self.file = open(filepath, "w")

    def startObject(self):
        self.newline()
        self.writeTabs()
        self.file.write("{ ") # additional space here so if no attributes endObject can still remove the last character
        self.inctab()

    def startObjectWithName(self, name):
        self.newline()
        self.writeTabs()
        self.file.write('"%s":{ ' % name)
        self.inctab()

    def attributeString(self, name, value):
        self.newline()
        self.writeTabs()
        self.file.write('"%s":"%s",' % (name, value))

    def attributeInt(self, name, value):
        self.newline()
        self.writeTabs()
        self.file.write('"%s":%i,' % (name, value))

    def attributeFloat(self, name, value):
        self.newline()
        self.writeTabs()
        self.file.write('"%s":%f,' % (name, value))

    def attributeVector3f(self, name, vec):
        self.startObjectWithName(name)
        self.attributeFloat("x", vec[0])
        self.attributeFloat("y", vec[1])
        self.attributeFloat("z", vec[2])
        self.endObject()

    def endObject(self):
        self.removeLastChar()
        self.newline()
        self.dectab()
        self.writeTabs()
        self.file.write("},")

    def startArray(self, name):
        self.newline()
        self.writeTabs()
        self.file.write('"%s":[ ' % name)
        self.inctab()

    def arrayFloat(self, value):
        self.newline()
        self.writeTabs()
        self.file.write("%f," % value)

    def endArray(self):
        self.removeLastChar()
        self.newline()
        self.dectab()
        self.writeTabs()
        self.file.write("],")

    def close(self):
        self.removeLastChar()
        self.file.close()

##########################################################################################
### Helper method for exporting properties of an orientated quad/plane
########################################################################################## 

def ExportQuadProperties(obj, json):
    mat = obj.matrix_world
    json.startArray("rotation")
    json.arrayFloat(mat[0][0])
    json.arrayFloat(mat[0][1])
    json.arrayFloat(mat[0][2])
    json.arrayFloat(mat[1][0])
    json.arrayFloat(mat[1][1])
    json.arrayFloat(mat[1][2])
    json.arrayFloat(mat[2][0])
    json.arrayFloat(mat[2][1])
    json.arrayFloat(mat[2][2])
    json.endArray();

    # calculate the door's object space dimensions in x and y
    minx = 10000000
    maxx = -10000000
    miny = 10000000
    maxy = -10000000
    for i in range(0, 8):
        vec = obj.bound_box[i]
        if vec[0] < minx:
            minx = vec[0]
        if vec[0] > maxx:
            maxx = vec[0]
        if vec[1] < miny:
            miny = vec[1]
        if vec[1] > maxy:
            maxy = vec[1]
            
    dimx = (maxx - minx)
    dimy = (maxy - miny)

    # centre position in object space
    pos = mathutils.Vector(((minx + maxx) * 0.5, (miny + maxy) * 0.5, 0.0))
    # transform into world space
    pos = obj.matrix_world * pos
    json.attributeVector3f("position", pos)

    json.attributeFloat("width", dimx)
    json.attributeFloat("height", dimy)

##########################################################################################
### Method for rendering six sides of a cubemap (used by ExportSector method below)
########################################################################################## 
def RenderCube(dstpath, loc):
    # Scene
    scene = bpy.data.scenes["Scene"]
    camera = scene.camera
    render = scene.render;
    
    camera.location = loc
    
    #set cam fov (must be 90 degrees)
    camera.data.angle = PI / 2.0
    
    camera.rotation_euler[0] = 0;
    camera.rotation_euler[1] = 0;
    camera.rotation_euler[2] = 0;
    
    cam_rot_x = [ 90, 90, 90, 90, 0, 180 ]
    cam_rot_z = [ 0, 90, 180, 270, 0, 0 ]
    names = [ "posy", "negx", "negy", "posx", "negz", "posz" ]
    
    # save any render settings and restore after we are done
    prevResX = render.resolution_x
    prevResY = render.resolution_y
    prevResPercent = render.resolution_percentage
    prevAspectX = render.pixel_aspect_x
    prevAspectY = render.pixel_aspect_y
    prevFileFormat = render.image_settings.file_format
    prevFilePath = render.filepath
    
    
    # set output resolution
    render.resolution_x = 1024
    render.resolution_y = 1024
    render.resolution_percentage = 50.0
    # set aspect ratio
    render.pixel_aspect_x = 1.0
    render.pixel_aspect_y = 1.0
    # file format
    render.image_settings.file_format = 'JPEG'
    
    for side in range(0, 6):
        camera.rotation_euler[0] = cam_rot_x[side] * (PI / 180.0)
        camera.rotation_euler[1] = 0.0
        camera.rotation_euler[2] = cam_rot_z[side] * (PI / 180.0)

        render.filepath = "%s/%s.jpg" % (dstpath, names[side])
        bpy.ops.render.render(write_still=True)
        
        
    # now restore the render settings
    render.resolution_x = prevResX
    render.resolution_y = prevResY
    render.resolution_percentage = prevResPercent
    render.pixel_aspect_x = prevAspectX
    render.pixel_aspect_y = prevAspectY
    render.image_settings.file_format = prevFileFormat
    render.filepath = prevFilePath

##########################################################################################
### Main method for exporting an Acorn sector object
##########################################################################################   
def ExportSector(filepath, sector_obj, renderScene):
    sectorname = sector_obj.acorn_sector_name;
    sectorpath = "%s/%s" % (filepath, sectorname)
    if not os.path.exists(sectorpath):
        os.makedirs(sectorpath);
    if renderScene:
        RenderCube(sectorpath, sector_obj.location)
    
    # open the sector file
    json = JSONWriter("%s/%s" % (sectorpath, "sector.json"))
    json.startObject()

    # export sector position
    json.attributeVector3f("position", sector_obj.location)
    
    json.attributeString("ambient_sound", sector_obj.acorn_sector_sound)
    json.attributeString("enemy_sound", sector_obj.acorn_sector_enemy_sound)
    json.attributeString("cutscene_text", sector_obj.acorn_sector_cutscene_text)
    json.attributeString("cutscene_video", sector_obj.acorn_sector_cutscene_video)

    # export all doors that link this sector with another
    json.startArray("doors")
    for door in bpy.data.objects:
        if door.acorn_type == "door":
            sectorA = door.acorn_door_sector_a
            sectorB = door.acorn_door_sector_b
            isSectorA = sectorname == sectorA
            isSectorB = sectorname == sectorB
            
            isValid = True
            # check if the door is one-way or not, if it is one-way
            # then we can only go from a to b
            if isSectorB:
                if door.acorn_door_oneway:
                    isValid = False
            
            if isValid:
                # if this door links to the current sector (sectorname)
                # through either sectorA or sectorB
                if isSectorA or isSectorB:
                    # create a new json object
                    json.startObject()
                    if isSectorA:
                        json.attributeString("links_to", sectorB)
                    else:
                        json.attributeString("links_to", sectorA)

                    ExportQuadProperties(door, json)

                    json.endObject()
    json.endArray()
                
    # export all enemy spawn points
    json.startArray("spawn_points")
    for spawn in bpy.data.objects:
        if spawn.acorn_type == "spawn":
            spawnSector = spawn.acorn_spawn_sector
            if spawnSector == sectorname:
                json.startObject()
                json.attributeVector3f("position", spawn.location)
                json.startArray("spawn_list")
                for item in spawn.acorn_spawn_list:
                    json.startObject()
                    json.attributeString("type", item.enemy_type)
                    json.attributeFloat("time", item.spawn_time)
                    json.endObject()
                json.endArray()
                json.endObject()       
    json.endArray()

    # export all weapon locations
    json.startArray("weapon_store_points")
    for weapon in bpy.data.objects:
        if weapon.acorn_type == "weapon":
            weaponSector = weapon.acorn_weapon_sector
            if weaponSector == sectorname:
                json.startObject()
                json.attributeString("weapon_type", weapon.acorn_weapon_type)
                json.attributeVector3f("position", weapon.location)
                json.attributeFloat("radius", weapon.dimensions[0] / 2.0)
                json.endObject()
    json.endArray()

    json.endObject()
    json.close()


##########################################################################################
### Acorn Scene Export Operator, exports all sectors to a specific location
##########################################################################################

def ExportAllFunction(filepath):
    if SwitchToExportCamera():
        HideAllAcornObjects()
        # for all render targets
        for object in bpy.data.objects:
            # if this object is an Acorn Sector
            if object.acorn_type == "sector":
                ExportSector(filepath, object, True)
        ShowAllAcornObjects()
    else:
        print("No Export camera in scene; set the camera name you wish to use for export as 'ExportCamera'")

### Operator
class ExportAll(bpy.types.Operator, ExportHelper):
    bl_idname = "acorn.export"
    bl_label = "Export Acorn Scene"
    
    filename_ext = ""

    ### Main Export Function
    def execute(self, context):
        # create a directory with the specified export name
        if not os.path.exists(self.filepath):
            os.makedirs(self.filepath)
            
        ExportAllFunction(self.filepath)
            
        return {"FINISHED"}

##########################################################################################
### Export all without performing a full re-render, just export sector.json files
########################################################################################## 
class ExportAllNoRender(bpy.types.Operator, ExportHelper):
    bl_idname = "acorn.export_no_render"
    bl_label = "Export No Render"
    
    filename_ext = ""

    ### Main Export Function
    def execute(self, context):
        if SwitchToExportCamera():
            # create a directory with the specified export name
            if not os.path.exists(self.filepath):
                os.makedirs(self.filepath)
                
            HideAllAcornObjects()
            # for all render targets
            for object in bpy.data.objects:
                # if this object is an Acorn Sector
                if object.acorn_type == "sector":
                    ExportSector(self.filepath, object, False)
            ShowAllAcornObjects()
            
        return {"FINISHED"}

##########################################################################################
### Operator for exporting the selcted sector
##########################################################################################
class ExportSelectedSector(bpy.types.Operator, ExportHelper):
    bl_idname = "acorn.export_selected"
    bl_label = "Export Selected Sector"
    
    filename_ext = ""
    
    ### Main Export Function
    def execute(self, context):
        if SwitchToExportCamera():
            # create a directory with the specified export name
            if not os.path.exists(self.filepath):
                os.makedirs(self.filepath)
                
            HideAllAcornObjects()
            object = bpy.context.active_object
            # if this object is an Acorn Sector
            if object.acorn_type == "sector":
                ExportSector(self.filepath, object, True)
            ShowAllAcornObjects()
            
        return {"FINISHED"}
    

##########################################################################################
### Operator for adding a new Acorn Sector to the Scene
##########################################################################################
class AddSector(bpy.types.Operator):
    bl_idname = "acorn.add_sector"
    bl_label = "Add Sector"
    
    def execute(self, context):
        # add a primitive cube that represents the render target
        bpy.ops.mesh.primitive_cube_add()
        cube = bpy.context.active_object
        cube.scale[0] = gSectorSize
        cube.scale[1] = gSectorSize
        cube.scale[2] = gSectorSize
        cube.acorn_type = "sector"
        # assign it a custom property so we know
        cube.acorn_sector_name = "<Sector Name>";
        return {"FINISHED"}

##########################################################################################
### Operator for adding a new Acorn Door to the Scene
##########################################################################################
class AddDoor(bpy.types.Operator):
    bl_idname = "acorn.add_door"
    bl_label = "Add Door"
    
    def execute(self, context):
        sector = bpy.context.active_object
        bpy.ops.mesh.primitive_plane_add()
        door = bpy.context.active_object
        door.acorn_type = "door"
        door.acorn_door_oneway = True
        # assign the names of the two sectors this door
        # connects
        door.acorn_door_sector_a = "<SectorA>"
        if sector is not None:
            if sector.acorn_type == "sector":
                door.acorn_door_sector_a = sector.acorn_sector_name;
        door.acorn_door_sector_b = "<SectorB>"
        return {"FINISHED"}

##########################################################################################
### Operator for adding a new Acorn Enemy Spawn Point to the Scene
##########################################################################################
class AddEnemySpawnPoint(bpy.types.Operator):
    bl_idname = "acorn.add_enemy_spawn"
    bl_label = "Add Spawn"
    
    def execute(self, context):
        sector = bpy.context.active_object
        bpy.ops.mesh.primitive_cylinder_add()
        spawn = bpy.context.active_object
        spawn.scale[0] = gSpawnSizeXY
        spawn.scale[1] = gSpawnSizeXY
        spawn.scale[2] = gSpawnSizeZ
        spawn.acorn_type = "spawn";
        spawn.acorn_spawn_sector = "";
        # if the user has the sector selected then automatically assign
        # the sector name to the spawn point
        if sector is not None:
            if sector.acorn_type == "sector":
                spawn.acorn_spawn_sector = sector.acorn_sector_name;
        return {"FINISHED"}

##########################################################################################
### Operator for adding a new Acorn Weapon Location to the Scene
##########################################################################################
class AddWeaponLocation(bpy.types.Operator):
    bl_idname = "acorn.add_weapon_location"
    bl_label = "Add Weapon"

    def execute(self, context):
        sector = bpy.context.active_object
        bpy.ops.mesh.primitive_uv_sphere_add()
        weapon = bpy.context.active_object
        weapon.acorn_type = "weapon";
        weapon.acorn_weapon_sector = "";
        # if the user has the sector selected then automatically assign
        # the sector name to the spawn point
        if sector is not None:
            if sector.acorn_type == "sector":
                weapon.acorn_weapon_sector = sector.acorn_sector_name;
        return {"FINISHED"}
        
##########################################################################################
### Operator for moving all acorn objects to a different layer
##########################################################################################
class HideAllAcornObjectsOperator(bpy.types.Operator):
    bl_idname = "acorn.hide_all_objects"
    bl_label = "Hide All"
    
    def execute(self, context):
        HideAllAcornObjects()
        return {"FINISHED"}
        
##########################################################################################
### Operator for moving all acorn objects to a different layer
##########################################################################################
class ShowAllAcornObjectsOperator(bpy.types.Operator):
    bl_idname = "acorn.show_all_objects"
    bl_label = "Show All"
    
    def execute(self, context):
        ShowAllAcornObjects()
        return {"FINISHED"}
      
      
##########################################################################################
### Code to Manage the Spawn List collection property
##########################################################################################
class SpawnListItem(bpy.types.PropertyGroup):
    enemy_type = bpy.props.StringProperty(name="enemy_type", default="BasicZombie")
    spawn_time = bpy.props.FloatProperty(name="spawn_time", default=0.0)
    
class AddSpawnListItem(bpy.types.Operator):
    bl_idname = "acorn.add_spawn_list_item"
    bl_label = "Add"
    
    def execute(self, context):
        spawn = bpy.context.active_object
        if spawn.acorn_type == "spawn":
            item = spawn.acorn_spawn_list.add()
            spawn.acorn_spawn_list_index = len(spawn.acorn_spawn_list) - 1
        return {"FINISHED"}
      
class RemoveSpawnListItem(bpy.types.Operator):
    bl_idname = "acorn.remove_spawn_list_item"
    bl_label = "Remove"
    
    def execute(self, context):
        spawn = bpy.context.active_object
        if spawn.acorn_type == "spawn":
            spawn.acorn_spawn_list.remove(spawn.acorn_spawn_list_index)
            spawn.acorn_spawn_list_index = spawn.acorn_spawn_list_index - 1
        return {"FINISHED"}
      
    
class SpawnListItemUI(bpy.types.UIList):
    #   The draw_item function is called for each item of the collection that is visible in the list.
    #   data is the RNA object containing the collection,
    #   item is the current drawn item of the collection,
    #   icon is the "computed" icon for the item (as an integer, because some objects like materials or textures
    #   have custom icons ID, which are not available as enum items).
    #   active_data is the RNA object containing the active property for the collection (i.e. integer pointing to the
    #   active item of the collection).
    #   active_propname is the name of the active property (use 'getattr(active_data, active_propname)').
    #   index is index of the current item in the collection.
    def draw_item(self, context, layout, data, item, icon, active_data, active_propname, index):
        # draw_item must handle the three layout types... Usually 'DEFAULT' and 'COMPACT' can share the same code.
        if self.layout_type in {'DEFAULT', 'COMPACT'}:
            # You should always start your row layout by a label (icon + text), this will also make the row easily
            # selectable in the list!
            # We use icon_value of label, as our given icon is an integer value, not an enum ID.
            layout.prop(item, "enemy_type", text="Enemy Type")
            # And now we can add other UI stuff...
            layout.prop(item, "spawn_time", text="Spawn Time")
        # 'GRID' layout type should be as compact as possible (typically a single icon!).
        elif self.layout_type in {'GRID'}:
            layout.alignment = 'CENTER'
            layout.label("", icon_value=icon)
      

##########################################################################################
### The Main Interfacing UI Panel for this Plugin
##########################################################################################
class MainPanel(bpy.types.Panel):
    #Creates a Panel in the Object properties window
    bl_label = "Acorn Plugin"
    bl_idname = "WORLD_PT_sector_editor"
    bl_space_type = 'PROPERTIES'
    bl_region_type = 'WINDOW'
    bl_context = "world"

    def draw(self, context):
        layout = self.layout

        row = layout.row()
        row.operator(ExportAll.bl_idname)

        row = layout.row()
        row.operator(ExportSelectedSector.bl_idname)
        
        row = layout.row()
        row.operator(ExportAllNoRender.bl_idname)

        row = layout.row()
        row.operator(AddSector.bl_idname)
        
        row = layout.row()
        row.operator(HideAllAcornObjectsOperator.bl_idname)
        row.operator(ShowAllAcornObjectsOperator.bl_idname)

        layout.separator()

        obj = bpy.context.active_object
        if obj is not None:
            ### Sector Panel
            if obj.acorn_type == "sector":
                box = layout.box()
                row = box.row()
                row.label("Sector", icon="GAME")
                row = box.row()
                row.prop(obj, "acorn_sector_name", text="Name")
                row = box.row()
                row.prop(obj, "acorn_sector_sound", text="Ambient Sound")
                row = box.row()
                row.prop(obj, "acorn_sector_enemy_sound", text="Enemy Sound")
                row = box.row()
                row.prop(obj, "acorn_sector_cutscene_video", text="Cutscene Video")
                row = box.row()
                row.prop(obj, "acorn_sector_cutscene_text", text="Cutscene Text")
                row = box.row()
                row.operator(AddDoor.bl_idname)
                row.operator(AddEnemySpawnPoint.bl_idname)
                row.operator(AddWeaponLocation.bl_idname)
                row = box.row()
                row.prop(obj, "location")

            ### Door Panel
            elif obj.acorn_type == "door":
                box = layout.box()
                row = box.row()
                row.label("Door", icon="PLUGIN")
                row = box.row()
                row.prop(obj, "acorn_door_sector_a", text="Sector A")
                row = box.row()
                row.prop(obj, "acorn_door_sector_b", text="Sector B")
                row = box.row()
                row.prop(obj, "acorn_door_oneway", text="One way")
                row = box.row()
                row.prop(obj, "location")

            ### Enemy Spawn Panel
            elif obj.acorn_type == "spawn":
                box = layout.box()
                row = box.row()
                row.label("Spawn point", icon="GAME")
                row = box.row()
                row.prop(obj, "acorn_spawn_sector", text="Sector")
                row = box.row()
                row.template_list("SpawnListItemUI", "", obj, "acorn_spawn_list", obj, "acorn_spawn_list_index")
                row = box.row()
                row.operator(AddSpawnListItem.bl_idname)
                row.operator(RemoveSpawnListItem.bl_idname)
                row = box.row()
                row.prop(obj, "location")

            ### Weapon location panel
            elif obj.acorn_type == "weapon":
                box = layout.box()
                row = box.row()
                row.label("Weapon Type", icon="PLUGIN")
                row = box.row()
                row.prop(obj, "acorn_weapon_sector", text="Sector")
                row = box.row()
                row.prop(obj, "acorn_weapon_type", text="Weapon Type")
                row = box.row()
                row.prop(obj, "location")



##########################################################################################
### Register all custom properties and Classes for this plugin
##########################################################################################
def register():
    # Acorn object types
    acorn_types = [ ("none", "None", "None"),
                    ("sector", "Sector", "Sector Desc"),
                    ("door", "Door", "Door Desc"),
                    ("spawn", "Spawn", "Spawn Desc"),
                    ("weapon", "Weapon", "Weapon Desc")]
    bpy.types.Object.acorn_type = bpy.props.EnumProperty(name="acorn_type", items=acorn_types, default="none")

    # Sector props
    bpy.types.Object.acorn_sector_name = bpy.props.StringProperty()
    bpy.types.Object.acorn_sector_sound = bpy.props.StringProperty(default="default_ambient")
    bpy.types.Object.acorn_sector_enemy_sound = bpy.props.StringProperty(default="default_enemy")
    bpy.types.Object.acorn_sector_cutscene_video = bpy.props.StringProperty(default="")
    bpy.types.Object.acorn_sector_cutscene_text = bpy.props.StringProperty(default="")

    # Door props
    bpy.types.Object.acorn_door_sector_a = bpy.props.StringProperty()
    bpy.types.Object.acorn_door_sector_b = bpy.props.StringProperty()
    bpy.types.Object.acorn_door_oneway = bpy.props.BoolProperty()

    bpy.utils.register_class(SpawnListItem)
    bpy.utils.register_class(SpawnListItemUI)
    bpy.utils.register_class(AddSpawnListItem)
    bpy.utils.register_class(RemoveSpawnListItem)

    # Spawn props
    bpy.types.Object.acorn_spawn_sector = bpy.props.StringProperty()
    bpy.types.Object.acorn_spawn_list = bpy.props.CollectionProperty(type=SpawnListItem)
    bpy.types.Object.acorn_spawn_list_index = bpy.props.IntProperty()

    # Weapon props
    bpy.types.Object.acorn_weapon_sector = bpy.props.StringProperty()
    bpy.types.Object.acorn_weapon_type = bpy.props.StringProperty()

    bpy.utils.register_class(ExportAll)
    bpy.utils.register_class(ExportSelectedSector)
    bpy.utils.register_class(ExportAllNoRender)
    bpy.utils.register_class(AddSector)
    bpy.utils.register_class(AddDoor)
    bpy.utils.register_class(AddEnemySpawnPoint)
    bpy.utils.register_class(AddWeaponLocation)
    bpy.utils.register_class(HideAllAcornObjectsOperator)
    bpy.utils.register_class(ShowAllAcornObjectsOperator)
    bpy.utils.register_class(MainPanel)
    

##########################################################################################
### Unregisters all classes of this plugin
##########################################################################################
def unregister():
    bpy.utils.unregister_class(ExportAll)
    bpy.utils.unregister_class(ExportSelectedSector)
    bpy.utils.unregister_class(ExportAllNoRender)
    bpy.utils.unregister_class(AddSector)
    bpy.utils.unregister_class(AddDoor)
    bpy.utils.unregister_class(AddEnemySpawnPoint)
    bpy.utils.unregister_class(AddWeaponLocation)
    bpy.utils.unregister_class(HideAllAcornObjectsOperator)
    bpy.utils.unregister_class(ShowAllAcornObjectsOperator)
    bpy.utils.unregister_class(MainPanel)


if __name__ == "__main__":
    register()





