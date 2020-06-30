#
#  Be sure to run `pod spec lint totvs-camera-core.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see https://guides.cocoapods.org/syntax/podspec.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|
  s.name         = "CameraCore"
  s.version      = "0.0.1"
  s.summary      = "Core classes for TOTVS camera capabilities"

  # This description is used to generate tags and improve search results.
  #   * Think: What does it do? Why did you write it? What is the focus?
  #   * Try to keep it short, snappy and to the point.
  #   * Write the description between the DESC delimiters below.
  #   * Finally, don't worry about the indent, CocoaPods strips it!
  s.description  = <<-DESC
  TODO: Add long description of the pod here.  
                   DESC

s.homepage      = "https://github.com/totvslabs/samples-mobile"
s.license       = 'MIT'
s.author        = { "Jansel Rodriguez" => "jvra16@gmail.com" }
s.source        = { :git => "https://github.com/totvslabs/samples-mobile.git", :tag => "#{s.version}" }
s.source_files  = "CameraCore/**/*.{swift,h,m}"
  
s.ios.deployment_target = '8.0'
s.swift_version = '5.0'

end
