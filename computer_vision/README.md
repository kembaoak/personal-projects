# Computer Vision — Facial Recognition (OpenCV)

Real-time facial recognition using your laptop's webcam, OpenCV's Haar cascade
face detector, and an LBPH (Local Binary Patterns Histograms) recognizer.

## Setup

```bash
cd computer_vision
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

On macOS, the first run will prompt for camera permission — grant it to
Terminal/iTerm/your IDE in System Settings → Privacy & Security → Camera.

## Usage

Run the three steps in order:

1. **Capture face samples** for each person you want recognized:
   ```bash
   python 1_capture_faces.py --name "YourName" --samples 50
   ```
   Repeat for each additional person. Images are saved under `dataset/`.

2. **Train the recognizer**:
   ```bash
   python 2_train_model.py
   ```
   This produces `trainer/trainer.yml`.

3. **Run live recognition**:
   ```bash
   python 3_recognize_faces.py
   ```
   Press `q` to quit. Adjust `--confidence-threshold` (lower = stricter
   match) if too many faces are labeled "Unknown" or matched incorrectly.

## Notes

- This uses Haar cascades for detection (fast, built into OpenCV) and LBPH
  for recognition (fast, works well with modest amounts of training data,
  ships in `opencv-contrib-python`).
- `dataset/` and `trainer/` will contain photos/biometric data of real
  people — keep this folder private and don't commit it to a public repo.
