import os

# Define the folder path
folder_path = 'camping'

# Function to recursively replace strings in files
def replace_string_in_files(folder):
    # Iterate over each file and folder in the current directory
    for root, dirs, files in os.walk(folder):
        for file in files:

            print(file)
            
            # Construct full file path
            file_path = os.path.join(root, file)

            # Check if it's a file
            if os.path.isfile(file_path):
                # Read the file
                with open(file_path, 'r', encoding='utf-8') as f:
                    file_data = f.read()

                # Replace the string
                file_data = file_data.replace('travelersbackpack', 'camping')

                # Write the modified content back to the file
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(file_data)

# Call the function on the root folder
replace_string_in_files(folder_path)

print("String replacement completed.")
