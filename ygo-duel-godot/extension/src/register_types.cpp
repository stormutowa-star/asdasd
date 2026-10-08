#include <gdextension_interface.h>

#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/core/defs.hpp>
#include <godot_cpp/godot.hpp>

#include "ygo_card_database.h"
#include "ygo_duel.h"

using namespace godot;

void initialize_ocgcore_module(ModuleInitializationLevel p_level) {
	if (p_level != MODULE_INITIALIZATION_LEVEL_SCENE)
		return;
	GDREGISTER_CLASS(YGOCardDatabase);
	GDREGISTER_CLASS(YGODuel);
}

void uninitialize_ocgcore_module(ModuleInitializationLevel p_level) {
}

extern "C" {
GDExtensionBool GDE_EXPORT ocgcore_gd_library_init(GDExtensionInterfaceGetProcAddress p_get_proc_address,
		const GDExtensionClassLibraryPtr p_library, GDExtensionInitialization *r_initialization) {
	godot::GDExtensionBinding::InitObject init_obj(p_get_proc_address, p_library, r_initialization);
	init_obj.register_initializer(initialize_ocgcore_module);
	init_obj.register_terminator(uninitialize_ocgcore_module);
	init_obj.set_minimum_library_initialization_level(MODULE_INITIALIZATION_LEVEL_SCENE);
	return init_obj.init();
}
}
