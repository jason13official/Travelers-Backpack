import os

# Define the folder path
folder_path = 'trinkets/entities'

# Iterate over each file in the folder
for filename in os.listdir(folder_path):
    # Construct full file path
    file_path = os.path.join(folder_path, filename)

    # Check if it's a file
    if os.path.isfile(file_path):
        # Read the file
        with open(file_path, 'r', encoding='utf-8') as file:
            file_data = file.read()

        # Replace the string
        file_data = file_data.replace('camping', 'camping')

        # Write the modified content back to the file
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(file_data)

print("String replacement completed.")
