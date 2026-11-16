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
			BRAK,
			PRZEKAZNIK,
			ROLETA,
			PRZYCISK,
			PRZYCISK_ROLETA,
			TERMOMETR,
			HIGROMETR
		};
		Device();
		Device(TYPE type);
		Device(TYPE type, byte id);
		Device(const Device & d);
		~Device();

		void setType(TYPE type);
		TYPE getType();
		void setId(byte id);
		byte getId();

	private:
	///typ urządzenia
	TYPE type_of_device;
	///id urzadzenia
	byte id;
};
#endif