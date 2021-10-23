#ifndef DEVICE_H
#define DEVICE_H
#include <Arduino.h>
///@author Marek Pałdyna
///Klasa podstawowa dla każdego urządzenia
class Device  
{
	
	public:
		enum TYPE
		{
			PRZEKAZNIK,
			ROLETA,
			PRZYCISK,
			PRZYCISK_ROLETA,
			TERMOMETR
		};
		Device();
		Device(TYPE type);
		Device(TYPE type, byte id);
		~Device();

		void setType(TYPE type);
		TYPE getType();
		void setId(byte id);
		byte getId();

	private:
	//typ konkretnego urządzenia
	TYPE type_of_device;
	byte id;
};
#endif