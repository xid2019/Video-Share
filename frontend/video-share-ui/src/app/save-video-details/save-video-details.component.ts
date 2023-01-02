import {Component} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {COMMA, ENTER} from "@angular/cdk/keycodes";
import {MatChipInputEvent} from "@angular/material/chips";
import {VideoService} from "../video.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ActivatedRoute} from "@angular/router";
import {BehaviorSubject, Subscription} from "rxjs";
import {VideoDto} from "../video-dto";

@Component({
  selector: 'app-save-video-details',
  templateUrl: './save-video-details.component.html',
  styleUrls: ['./save-video-details.component.css']
})
export class SaveVideoDetailsComponent {

  saveVideoForm: FormGroup;
  title: FormControl = new FormControl('');
  description: FormControl = new FormControl('');
  videoStatus: FormControl = new FormControl('')
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  tags: string[] = [];
  showVideoUrl = false;
  videoUrlAvailable = false;
  videoUrl!: string;
  thumbnailUrl!: string;
  videoId!: string;
  selectedFile!: File;
  selectedFileName = '';
  uploadThumbnailSubscription!: Subscription;
  fileUploaded!: boolean;
  thumbnailUploaded: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private videoService: VideoService, private matSnackBar: MatSnackBar,
              private route: ActivatedRoute) {
    this.videoId = this.route.snapshot.params['videoId'];
    this.videoService.getVideo(this.videoId).subscribe(data => {
      this.videoUrl = data.videoUrl;
      this.thumbnailUrl = data.thumbnailUrl;
      this.videoUrlAvailable = true;
    })
    this.saveVideoForm = new FormGroup({
      title: this.title,
      description: this.description,
      videoStatus: this.videoStatus,
    })
  }

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      this.tags.push(value.trim());
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  remove(value: string): void {
    const index = this.tags.indexOf(value);

    if (index >= 0) {
      this.tags.splice(index, 1);
    }
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    this.selectedFileName = this.selectedFile.name;
  }

  onUpload() {
    this.uploadThumbnailSubscription = this.videoService.uploadThumbnail(this.selectedFile, this.videoId)
      .subscribe(data => {
        this.thumbnailUploaded.subscribe(() => {
          this.matSnackBar.open("Thumbnail Uploaded Successfully", "OK");
          this.fileUploaded = true;
        });
      });
  }

  saveVideo() {
    const videoMetData: VideoDto = {
      "id": this.videoId,
      "title": this.saveVideoForm.get('title')?.value,
      "description": this.saveVideoForm.get('description')?.value,
      "tags": this.tags,
      "videoStatus": this.saveVideoForm.get('videoStatus')?.value,
      "videoUrl": this.videoUrl,
      "thumbnailUrl": this.thumbnailUrl,
      "likeCount": 0,
      "dislikeCount": 0,
      "viewCount": 0
    }
    this.videoService.saveVideo(videoMetData).subscribe(data => {
      this.matSnackBar.open("Video Metadata Updated successfully", "OK")
    })
  }
}
