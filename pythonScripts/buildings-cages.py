import random

def generate_buildings(file_path, count=5):
    streets = ["Main-St", "Oak-Ave", "Pine-Rd", "Maple-Ln", "Elm-Blvd"]
    with open(file_path, 'w') as f:
        for i in range(count):
            address = f"{random.choice(streets)}/{random.randint(0, 99)}"
            phone = f"+48--{random.randint(000, 999)}-{random.randint(000, 999)}-{random.randint(000, 999)}"
            line = f"true {address} {phone}\n"
            f.write(line)


def generate_cages(file_path, buildings_count=5, cages_per_building=8):
    with open(file_path, 'w') as f:
        for building_id in range(1, buildings_count + 1):
            for _ in range(cages_per_building):
                section = random.randint(1, cages_per_building)
                size = random.randint(1, 5)
                line = f"{section} {size} {building_id}\n"
                f.write(line)


buildings_file = "buildings.txt"
cages_file = "cages.txt"


generate_buildings(buildings_file)
generate_cages(cages_file)