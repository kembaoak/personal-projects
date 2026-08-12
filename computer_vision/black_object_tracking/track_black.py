"""
Real-time black object tracking for objects held close to the camera.

Black isn't a hue like yellow — it's any pixel with low brightness (V),
regardless of hue/saturation, so the mask thresholds on brightness alone.

Detection runs on a downscaled copy of each frame (faster than full-res
processing, so it keeps up with the camera's frame rate), and the box
position is smoothed across frames (exponential moving average) so it
eases toward new detections instead of snapping/jittering.

Usage:
    python track_black.py
    python track_black.py --camera 1 --min-area 3000
    python track_black.py --smoothing 0.5

Press 'q' to quit.
"""

import argparse

import cv2
import numpy as np

# HSV range for black: full hue/saturation range, low value (brightness).
LOWER_BLACK = np.array([0, 0, 0])
UPPER_BLACK = np.array([180, 255, 60])

PROCESS_WIDTH = 400  # frames are downscaled to this width before analysis
MAX_MISSED_FRAMES = 10  # clear the box after this many frames with no detection


def main():
    parser = argparse.ArgumentParser(description="Real-time black object tracking.")
    parser.add_argument("--camera", type=int, default=0, help="Camera index.")
    parser.add_argument(
        "--min-area",
        type=int,
        default=3000,
        help=(
            "Minimum contour area (pixels, at full frame scale) to count as "
            "a tracked object. Higher than the yellow tracker's default "
            "since a close-up object fills more of the frame."
        ),
    )
    parser.add_argument(
        "--smoothing",
        type=float,
        default=0.4,
        help=(
            "Exponential smoothing factor for the box position, 0-1. "
            "Higher = snappier/less smoothed, lower = smoother/more lag."
        ),
    )
    args = parser.parse_args()

    cam = cv2.VideoCapture(args.camera, cv2.CAP_AVFOUNDATION)
    if not cam.isOpened():
        raise RuntimeError(
            f"Could not open camera index {args.camera}. "
            "Try a different --camera value (0, 1, ...)."
        )

    # macOS's AVFoundation backend returns black frames for the first
    # moment while the sensor warms up; discard a few before starting.
    for _ in range(30):
        cam.read()

    print("[INFO] Tracking black objects. Press 'q' to quit.")

    smoothed_box = None  # (x, y, w, h) in full-frame coordinates
    missed_frames = 0

    while True:
        ok, frame = cam.read()
        if not ok:
            print("[WARN] Failed to read frame from camera.")
            break

        frame_h, frame_w = frame.shape[:2]
        scale = frame_w / PROCESS_WIDTH
        small = cv2.resize(
            frame, (PROCESS_WIDTH, int(frame_h / scale)), interpolation=cv2.INTER_AREA
        )

        blurred = cv2.GaussianBlur(small, (11, 11), 0)
        hsv = cv2.cvtColor(blurred, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, LOWER_BLACK, UPPER_BLACK)
        mask = cv2.erode(mask, None, iterations=2)
        mask = cv2.dilate(mask, None, iterations=2)

        contours, _ = cv2.findContours(
            mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE
        )

        detected = None
        if contours:
            largest = max(contours, key=cv2.contourArea)
            # Compare area in full-frame scale against --min-area.
            if cv2.contourArea(largest) * (scale ** 2) >= args.min_area:
                x, y, w, h = cv2.boundingRect(largest)
                detected = (x * scale, y * scale, w * scale, h * scale)

        if detected is not None:
            missed_frames = 0
            if smoothed_box is None:
                smoothed_box = detected
            else:
                alpha = args.smoothing
                smoothed_box = tuple(
                    alpha * new + (1 - alpha) * old
                    for new, old in zip(detected, smoothed_box)
                )
        else:
            missed_frames += 1
            if missed_frames > MAX_MISSED_FRAMES:
                smoothed_box = None

        if smoothed_box is not None:
            x, y, w, h = (int(v) for v in smoothed_box)
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
            cv2.putText(
                frame,
                "Black object",
                (x, y - 10),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.7,
                (0, 0, 255),
                2,
            )

        cv2.imshow("Black Object Tracking - press q to quit", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cam.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
