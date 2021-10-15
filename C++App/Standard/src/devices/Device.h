#ifndef DEVICE_H
#define DEVICE_H

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
		~Device();

		void setType(TYPE type);
		TYPE getType();
	private:
	//typ konkretnego urządzenia
	TYPE type_of_device;
};
#endif